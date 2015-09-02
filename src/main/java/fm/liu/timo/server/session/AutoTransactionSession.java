package fm.liu.timo.server.session;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.route.Outlets;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.server.session.handler.AutoTransactionHandler;
import fm.liu.timo.server.session.handler.ResultHandler;
import fm.liu.timo.server.session.handler.RollbackHandler;
import fm.liu.timo.server.session.handler.SessionResultHandler;

/**
 * @author liuhuanting
 */
public class AutoTransactionSession extends AbstractSession {

    public AutoTransactionSession(ServerConnection front) {
        super(front);
        variables.setAutocommit(false);
    }

    @Override
    protected SessionResultHandler chooseHandler(Outlets outs, int type) {
        return new AutoTransactionHandler(this, outs.size());
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

}
