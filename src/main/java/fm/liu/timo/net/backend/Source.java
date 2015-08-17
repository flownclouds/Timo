/*
 * Copyright 2015 Liu Huanting.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package fm.liu.timo.net.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import fm.liu.timo.config.model.Datasource;
import fm.liu.timo.config.model.Datasource.Status;
import fm.liu.timo.heartbeat.Heartbeat;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.net.connection.AbstractConnection.State;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.net.connection.Variables;
import fm.liu.timo.net.factory.BackendConnectionFactory;
import fm.liu.timo.net.handler.BackendConnectHandler;
import fm.liu.timo.net.handler.BackendCreateConnectionHandler;
import fm.liu.timo.net.handler.InnerCreateConnectionHandler;
import fm.liu.timo.server.session.handler.ResultHandler;
import fm.liu.timo.server.session.handler.VirtualHandler;
import fm.liu.timo.util.TimeUtil;

/**
 * 数据源
 * @author Liu Huanting 2015年5月9日
 */
public class Source {
    private Datasource                                       config;
    private final int                                        datanodeID;
    private final BackendConnectionFactory                   factory;
    private final Heartbeat                                  heartbeat;
    private final ConcurrentHashMap<Long, BackendConnection> connections =
            new ConcurrentHashMap<Long, BackendConnection>();
    private final ConcurrentLinkedQueue<BackendConnection>   idle        =
            new ConcurrentLinkedQueue<BackendConnection>();

    public Source(Datasource config, int datanodeID, Variables variables, int heartbeatPeriod) {
        this.config = config;
        this.datanodeID = datanodeID;
        this.factory = new BackendConnectionFactory(variables) {};
        this.heartbeat = new Heartbeat(this, heartbeatPeriod);
    }

    public boolean init() {
        int size = config.getInitCon();
        BackendCreateConnectionHandler handler = new BackendCreateConnectionHandler();
        handler.setDB(config.getDB());
        for (int i = 0; i < size; i++) {
            this.newConnection(handler);
        }
        long timeout = System.currentTimeMillis() + 10 * 1000;
        while (handler.getFinished() < size && (System.currentTimeMillis() < timeout)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (idle.size() != size) {
            return false;
        } else {
            return true;
        }
    }

    public MySQLConnection newConnection(final BackendConnectHandler handler) {
        int size = getSize();
        if (size > config.getMaxCon()) {

        }
        MySQLConnection c = null;
        handler.setDB(config.getDB());
        try {
            c = factory.newMySQLConnection(handler, config, this);
        } catch (IOException e) {
            handler.error("create connection exception:" + e.getMessage(), null);
        }
        return c;
    }

    public boolean isAvailable() {
        return Status.NORMAL.equals(config.getStatus());
    }

    public int getSize() {
        return connections.size();
    }

    public int getIdleSize() {
        return idle.size();
    }

    public void remove(MySQLConnection con) {
        if (idle.contains(con)) {
            idle.remove(con);
        }
        connections.remove(con);
    }

    public void add(BackendConnection c) {
        connections.put(c.getID(), c);
    }

    public Datasource getConfig() {
        return config;
    }

    public int getDatanodeID() {
        return datanodeID;
    }

    public void release(BackendConnection con) {
        con.setState(State.idle);
        idle.offer(con);
    }

    public BackendConnection get() {
        BackendConnection con = idle.poll();
        if (con != null) {
            con.setState(State.borrowed);
        }
        return con;
    }

    public void idleCheck() {
        int idleSize = this.idle.size();
        int increase = config.getMinIdle() - idleSize;
        if (increase > 0) {
            increase = Math.min(increase, config.getMaxIdle() - this.getSize());
        }
        for (int i = 0; i < increase; i++) {
            BackendConnectHandler handler = new BackendCreateConnectionHandler();
            handler.setDB(config.getDB());
            this.newConnection(handler);
        }
        int decrease = idleSize - config.getMaxIdle();
        if (decrease > 0) {
            ArrayList<BackendConnection> connections = get(decrease);
            for (BackendConnection connection : connections) {
                connection.close();
            }
        }
        long lastActiveTime = TimeUtil.currentTimeMillis() - config.getIdleCheckPeriod();
        ArrayList<BackendConnection> connections = get(idleSize / 4);
        for (BackendConnection connection : connections) {
            if (connection.getVariables().getLastActiveTime() < lastActiveTime) {
                connection.query("SELECT 1", new VirtualHandler());
            } else {
                release(connection);
            }
        }
    }

    private ArrayList<BackendConnection> get(int decrease) {
        ArrayList<BackendConnection> connections = new ArrayList<BackendConnection>();
        while (!idle.isEmpty() && connections.size() < decrease) {
            BackendConnection connection = idle.poll();
            if (connection != null) {
                connections.add(connection);
            }
        }
        return connections;
    }

    public void query(String sql, ResultHandler handler) {
        BackendConnection conn = this.get();
        if (conn == null) {
            InnerCreateConnectionHandler innerHandler =
                    new InnerCreateConnectionHandler(sql, handler);
            this.newConnection(innerHandler);
        } else {
            conn.query(sql, handler);
        }
    }

    public void heartbeat(Node node) {
        if (isAvailable() && !heartbeat.isStoped()) {
            heartbeat.heartbeat(node);
        }
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    public void clear() {
        for (BackendConnection connection : connections.values()) {
            connection.close();
        }
        heartbeat.stop();
    }
}
