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
package re.ovo.timo.net.backend;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import re.ovo.timo.config.model.Datasource;
import re.ovo.timo.config.model.Datasource.Status;
import re.ovo.timo.mysql.connection.MySQLConnection;
import re.ovo.timo.net.connection.AbstractConnection.State;
import re.ovo.timo.net.connection.BackendConnection;
import re.ovo.timo.net.connection.Variables;
import re.ovo.timo.net.factory.BackendConnectionFactory;
import re.ovo.timo.net.handler.BackendConnectHandler;
import re.ovo.timo.net.handler.BackendCreateConnectionHandler;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class Source {
    private Datasource config;
    private final int datanodeID;
    private final BackendConnectionFactory factory;
    private final ConcurrentHashMap<Long, BackendConnection> connections =
            new ConcurrentHashMap<Long, BackendConnection>();
    private final ConcurrentLinkedQueue<BackendConnection> idle =
            new ConcurrentLinkedQueue<BackendConnection>();

    public Source(Datasource config, int datanodeID, Variables variables) {
        this.config = config;
        this.datanodeID = datanodeID;
        this.factory = new BackendConnectionFactory(variables) {};
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
    
    public boolean isAvailable(){
        return Status.NORMAL.equals(config.getStatus());
    }

    public int getSize() {
        return connections.size();
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
    public Datasource getConfig(){
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
}
