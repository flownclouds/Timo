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
package fm.liu.timo.server.session;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import fm.liu.timo.TimoConfig;
import fm.liu.timo.TimoServer;
import fm.liu.timo.backend.Node;
import fm.liu.timo.merger.Merger;
import fm.liu.timo.mysql.handler.MultiNodeHandler;
import fm.liu.timo.mysql.handler.SingleNodeHandler;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.net.connection.Variables;
import fm.liu.timo.route.Outlet;
import fm.liu.timo.route.Outlets;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.server.parser.ServerParse;
import fm.liu.timo.server.session.handler.SessionResultHandler;

/**
 * @author Liu Huanting 2015年5月9日
 */
public abstract class AbstractSession implements Session {
    protected final ServerConnection                              front;
    protected final ConcurrentHashMap<Integer, BackendConnection> connections;
    protected final Variables                                     variables;

    public AbstractSession(ServerConnection front) {
        this.front = front;
        this.connections = new ConcurrentHashMap<Integer, BackendConnection>();
        this.variables = new Variables();
        this.variables.setCharsetIndex(front.getVariables().getCharsetIndex());
        this.variables.setIsolationLevel(front.getVariables().getIsolationLevel());
    }

    @Override
    public ServerConnection getFront() {
        return front;
    }

    @Override
    public Collection<BackendConnection> getConnections() {
        return connections.values();
    }

    public Variables getVariables() {
        return variables;
    }

    @Override
    public Collection<BackendConnection> availableConnections() {
        Collection<BackendConnection> cons = new ArrayList<>();
        for (BackendConnection con : getConnections()) {
            if (!con.isClosed()) {
                cons.add(con);
            }
        }
        return cons;
    }

    @Override
    public final void offer(BackendConnection con) {
        connections.put(con.getDatanodeID(), con);
    }

    @Override
    public void execute(Outlets outs, int type) {
        boolean read = type == ServerParse.SELECT;
        boolean usingMaster = outs.usingMaster();
        SessionResultHandler handler = chooseHandler(outs, type);
        TimoConfig config = TimoServer.getInstance().getConfig();
        for (Outlet out : outs.getResult()) {
            String sql = out.getSql();
            handler.setSQL(sql);
            Node node = config.getNodes().get(out.getID());
            BackendConnection con = connections.get(out.getID());
            if (con != null) {
                offer(con);
                con.query(sql, handler);
            } else {
                if (usingMaster) {
                    node.getSource().query(out.getSql(), handler);
                } else {
                    node.query(out.getSql(), handler, read);
                }
            }
        }
    }

    protected SessionResultHandler chooseHandler(Outlets outs, int type) {
        int size = outs.size();
        if (1 == size) {
            return new SingleNodeHandler(this);
        }
        return new MultiNodeHandler(this, new Merger(outs), size);
    }

    public void commit() {
        ByteBuffer buffer = front.allocate();
        buffer = front.writeToBuffer(OkPacket.OK, buffer);
        front.write(buffer);
    }

    public void rollback() {
        ByteBuffer buffer = front.allocate();
        buffer = front.writeToBuffer(OkPacket.OK, buffer);
        front.write(buffer);
    }
}
