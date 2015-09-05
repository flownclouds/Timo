package fm.liu.timo.server.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.server.session.handler.CommitHandler;
import fm.liu.timo.server.session.handler.ResultHandler;
import fm.liu.timo.server.session.handler.RollbackHandler;
import fm.liu.timo.server.session.handler.RollbackToSavepointHandler;
import fm.liu.timo.server.session.handler.SavepointHandler;

/**
 * @author liuhuanting
 */
public class TransactionSession extends AbstractSession {
    private final String savepoint;
    private final String rollbackToSavepoint;

    public TransactionSession(ServerConnection front) {
        super(front);
        String tag = "savepoint" + this.hashCode();
        this.savepoint = "savepoint " + tag;
        this.rollbackToSavepoint = "rollback to savepoint " + tag;
        variables.setAutocommit(false);
    }

    public String getSavepoint() {
        return savepoint;
    }

    @Override
    public void release(BackendConnection con) {}

    @Override
    public void clear() {
        front.reset();
        ArrayList<BackendConnection> rollbacks = new ArrayList<>();
        KeySetView<Integer, BackendConnection> keys = connections.keySet();
        for (Integer id : keys) {
            BackendConnection con = connections.remove(id);
            if (con.isClosed()) {
                continue;
            }
            if (con.isRunning()) {
                con.setHandler(null);
                con.close("cleared");
            } else {
                rollbacks.add(con);
            }
        }
        ResultHandler handler = new RollbackHandler();
        for (BackendConnection con : rollbacks) {
            con.query("rollback", handler);
        }
    }

    @Override
    public void commit() {
        if (getConnections().isEmpty()) {
            super.commit();
            return;
        }
        Collection<BackendConnection> cons = availableConnections();
        if (cons.size() == getConnections().size()) {
            ResultHandler handler = new CommitHandler(this, cons);
            for (BackendConnection con : cons) {
                con.query("commit", handler);
            }
        } else {
            onError();
        }
    }

    @Override
    public void rollback(boolean response) {
        front.reset();
        this.clear();
        if (response) {
            super.rollback(response);
        }
    }

    public void savepoint(OkPacket ok) {
        Collection<BackendConnection> cons = availableConnections();
        if (cons.size() == getConnections().size()) {
            ResultHandler handler = new SavepointHandler(this, cons.size(), ok);
            for (BackendConnection con : cons) {
                con.query(savepoint, handler);
            }
        } else {
            onError();
        }
    }

    public void rollbackToSavepoint(ErrorPacket err) {
        Collection<BackendConnection> cons = availableConnections();
        if (cons.size() == getConnections().size()) {
            ResultHandler handler = new RollbackToSavepointHandler(this, cons.size(), err);
            for (BackendConnection con : cons) {
                con.query(rollbackToSavepoint, handler);
            }
        } else {
            onError();
        }
    }

    private void onError() {
        front.reset();
        this.clear();
        front.writeErrMessage(ErrorCode.ER_YES,
                "some connection already closed, transaction have been rollbacked automatically");
    }

}
