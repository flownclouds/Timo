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
package fm.liu.timo.server;

import java.nio.channels.SocketChannel;
import java.sql.SQLSyntaxErrorException;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.route.Outlets;
import fm.liu.timo.route.Router;
import fm.liu.timo.server.parser.ServerParse;
import fm.liu.timo.server.response.Heartbeat;
import fm.liu.timo.server.response.Ping;
import fm.liu.timo.server.session.AutoCommitSession;
import fm.liu.timo.server.session.Session;
import fm.liu.timo.server.session.TransactionSession;
import fm.liu.timo.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-21 上午11:22:57
 */
public class ServerConnection extends FrontendConnection {
    private static final long AUTH_TIMEOUT = 15 * 1000L;
    private volatile Session  session;
    private volatile Session  nextSession;
    private final Session     autocommitSession;
    private final Session     transactionSession;
    private final Session     autoTransactionSession;
    private long              lastInsertID;

    public ServerConnection(SocketChannel channel, NIOProcessor processor) {
        super(channel, processor);
        autocommitSession = new AutoCommitSession(this);
        transactionSession = new TransactionSession(this);
        autoTransactionSession = new AutoCommitSession(this);
        session = autocommitSession;
    }

    @Override
    public boolean isIdleTimeout() {
        if (isAuthenticated) {
            return super.isIdleTimeout();
        } else {
            return TimeUtil.currentTimeMillis() > variables.getLastActiveTime() + AUTH_TIMEOUT;
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
        if (!TimoServer.getInstance().isOnline()) {
            writeErrMessage(ErrorCode.ER_ACCESS_DENIED_ERROR, "Timo-server is offline");
            return;
        }
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
            out = Router.route(database, sql, this.getCharset(), type);
        } catch (SQLSyntaxErrorException e) {
            String msg = e.getMessage();
            writeErrMessage(ErrorCode.ER_PARSE_ERROR,
                    msg == null ? e.getClass().getSimpleName() : msg);
            return;
        }
        chooseSession(type);
        session.execute(out, type);
    }

    private void chooseSession(int type) {
        if (session instanceof TransactionSession) {
            return;
        }
        if (variables.isAutocommit()) {
            switch (type) {
                case ServerParse.INSERT:
                case ServerParse.DELETE:
                case ServerParse.UPDATE:
                case ServerParse.REPLACE:
                    session = autoTransactionSession;
            }
        }
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
    public void close(String reason) {
        if (super.closed.compareAndSet(false, true)) {
            this.processor.remove(this);
            if (session != null) {
                Session tmp = session;
                session = null;
                tmp.clear();
            }
            super.cleanup();
        }
    }

    public void setLastInsertId(long lastInsertID) {
        this.lastInsertID = lastInsertID;
    }

    public long getLastInsertId() {
        return lastInsertID;
    }

    public Session getSession() {
        return session;
    }

    public boolean setCharset(String charset) {
        boolean result = variables.setCharset(charset);
        if (result) {
            int index = variables.getCharsetIndex();
            autocommitSession.getVariables().setCharsetIndex(index);
            transactionSession.getVariables().setCharsetIndex(index);
            autoTransactionSession.getVariables().setCharsetIndex(index);
        }
        return result;
    }

    public void setIsolationLevel(int level) {
        variables.setIsolationLevel(level);
        autocommitSession.getVariables().setIsolationLevel(level);
        transactionSession.getVariables().setIsolationLevel(level);
        autoTransactionSession.getVariables().setIsolationLevel(level);
    }

    public void setAutocommit(boolean autocommit) {
        Session tmp = session;
        variables.setAutocommit(autocommit);
        if (autocommit) {
            tmp.commit();
        } else {
            if (!(tmp instanceof TransactionSession)) {
                session = transactionSession;
            }
            write(OkPacket.OK);
        }
    }

    public void startTransaction() {
        if (session instanceof TransactionSession && !session.getConnections().isEmpty()) {
            nextSession = transactionSession;
            session.commit();
        } else {
            session = transactionSession;
            write(OkPacket.OK);
        }
    }

    public void commit() {
        session.commit();
    }

    public void rollback() {
        session.rollback();
    }

    public void reset() {
        if (nextSession != null) {
            session = nextSession;
            nextSession = null;
            return;
        }
        if (this.getVariables().isAutocommit()) {
            session = autocommitSession;
        } else {
            session = transactionSession;
        }
    }

}
