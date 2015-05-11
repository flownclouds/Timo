/*
 * Copyright 1999-2012 Alibaba Group.
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
package re.ovo.timo.server;

import java.nio.channels.SocketChannel;
import java.sql.SQLSyntaxErrorException;

import re.ovo.timo.TimoServer;
import re.ovo.timo.config.ErrorCode;
import re.ovo.timo.config.model.Database;
import re.ovo.timo.net.NIOProcessor;
import re.ovo.timo.net.connection.FrontendConnection;
import re.ovo.timo.route.Outlets;
import re.ovo.timo.route.Router;
import re.ovo.timo.server.response.Heartbeat;
import re.ovo.timo.server.response.Ping;
import re.ovo.timo.server.session.AbstractSession;
import re.ovo.timo.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-21 上午11:22:57
 */
public class ServerConnection extends FrontendConnection {
    private static final long AUTH_TIMEOUT = 15 * 1000L;

    private AbstractSession session;

    public ServerConnection(SocketChannel channel, NIOProcessor processor) {
        super(channel, processor);
        session = new AbstractSession(this);
    }

    @Override
    public boolean isIdleTimeout() {
        if (isAuthenticated) {
            return super.isIdleTimeout();
        } else {
            return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime)
                    + AUTH_TIMEOUT;
        }
    }

    @Override
    public void ping() {
        Ping.response(this);
    }

    @Override
    public void heartbeat(byte[] data) {
        Heartbeat.response(this, data);
    }

    public void execute(String sql, int type) {
        // 检查当前使用的DB
        String db = this.db;
        if (db == null) {
            writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return;
        }
        Database database =
                TimoServer.getInstance().getConfig().getDatabases().get(db.toUpperCase());
        if (database == null) {
            writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return;
        }
        Outlets out = null;
        try {
            out = Router.route(database, sql, charset, type);
        } catch (SQLSyntaxErrorException e) {
            String msg = e.getMessage();
            writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.getClass().getSimpleName()
                    : msg);
            return;
        }
        session.execute(out, type);
    }

    /**
     * 撤销执行中的语句
     * 
     * @param sponsor 发起者为null表示是自己
     */
    public void cancel(final FrontendConnection sponsor) {
        processor.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // TODO kill query
            }
        });
    }

    @Override
    public void close() {
        if (super.closed.compareAndSet(false, true)) {
            super.cleanup();
        }
    }

    public long getLastInsertId() {
        // TODO Auto-generated method stub
        return 0;
    }

}
