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
package re.ovo.timo.net.handler;

import java.util.concurrent.atomic.AtomicInteger;

import re.ovo.timo.net.connection.BackendConnection;
import re.ovo.timo.route.RouteResultsetNode;
import re.ovo.timo.server.session.handler.OKResultHandler;
import re.ovo.timo.server.session.handler.ResultHandler;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class BackendCreateConnectionHandler implements BackendConnectHandler {
    private final AtomicInteger finished = new AtomicInteger(0);
    private String db;

    @Override
    public void error(String msg, BackendConnection con) {
        this.failed(msg, con);
    }

    public void failed(String msg, BackendConnection con) {
        finished.incrementAndGet();
    }

    @Override
    public void acquired(BackendConnection con) {
        ResultHandler handler = new InitDBHandler();
        RouteResultsetNode rrn = new RouteResultsetNode("", "USE " + db);
        con.query(rrn, handler);
    }

    @Override
    public void setDB(String db) {
        this.db = db;
    }

    public class InitDBHandler extends OKResultHandler {

        @Override
        protected void failed(BackendConnection con, String error) {
            connecFailed("caught unexpected row response:" + error, con);
        }

        @Override
        protected void success(BackendConnection con) {
            connectSuccess(con);
        }
    }

    private void connecFailed(String string, BackendConnection con) {
        finished.incrementAndGet();
    }

    private void connectSuccess(BackendConnection con) {
        finished.incrementAndGet();
        con.release();
    }
}
