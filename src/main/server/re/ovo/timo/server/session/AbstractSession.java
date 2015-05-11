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
package re.ovo.timo.server.session;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import re.ovo.timo.TimoConfig;
import re.ovo.timo.TimoServer;
import re.ovo.timo.mysql.handler.MySQLMultiNodeHandler;
import re.ovo.timo.mysql.handler.MySQLSingleNodeHandler;
import re.ovo.timo.net.backend.Node;
import re.ovo.timo.net.connection.BackendConnection;
import re.ovo.timo.net.connection.Variables;
import re.ovo.timo.route.Outlet;
import re.ovo.timo.route.Outlets;
import re.ovo.timo.server.ServerConnection;
import re.ovo.timo.server.session.handler.ResultHandler;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class AbstractSession {
    protected final ServerConnection front;
    protected final ConcurrentHashMap<Integer, BackendConnection> connections;
    protected final Variables variables;

    public AbstractSession(ServerConnection front) {
        this.front = front;
        this.connections = new ConcurrentHashMap<Integer, BackendConnection>();
        this.variables = new Variables();
        this.variables.setCharsetIndex(front.getVariables().getCharsetIndex());
        this.variables.setIsolationLevel(front.getVariables().getIsolationLevel());
    }

    public ServerConnection getFront() {
        return front;
    }

    public Collection<BackendConnection> getConnections() {
        return connections.values();
    }
    
    public final void offer(BackendConnection con){
        connections.put(con.getDatanodeID(), con);
    }
    
    public void execute(Outlets outs,int type){
        ResultHandler handler = chooseHandler(outs,type);
        TimoConfig config = TimoServer.getInstance().getConfig();
        for(Outlet out:outs.getResult()){
            Node node = config.getNodes().get(out.getID());
            BackendConnection con = node.getSource().get();
            con.query(out, handler);
        }
    }

    private ResultHandler chooseHandler(Outlets outs, int type) {
        int size = outs.size();
        if (1 == size) {
            return new MySQLSingleNodeHandler(this);
        }
        return new MySQLMultiNodeHandler(this, size);
    }
}
