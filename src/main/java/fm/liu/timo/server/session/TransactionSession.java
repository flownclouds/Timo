package fm.liu.timo.server.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.server.session.handler.CommitHandler;
import fm.liu.timo.server.session.handler.ResultHandler;
import fm.liu.timo.server.session.handler.RollbackHandler;

/**
 * @author liuhuanting
 */
public class TransactionSession extends AbstractSession {

    public TransactionSession(ServerConnection front) {
        super(front);
        variables.setAutocommit(false);
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
            commit(cons);
        } else {
            front.reset();
            this.clear();
            front.writeErrMessage(ErrorCode.ER_YES, "some connection already closed when commit");
        }
    }

    private void commit(Collection<BackendConnection> cons) {
        ResultHandler handler = new CommitHandler(this, cons);
        for (BackendConnection con : cons) {
            con.query("commit", handler);
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

}
