package fm.liu.timo.server.session;

import java.util.concurrent.ConcurrentHashMap.KeySetView;
import org.pmw.tinylog.Logger;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.ServerConnection;

/**
 * @author liuhuanting
 */
public class AutoCommitSession extends AbstractSession {

    public AutoCommitSession(ServerConnection front) {
        super(front);
    }

    @Override
    public void release(BackendConnection con) {
        if (con != null) {
            BackendConnection c = connections.remove(con.getDatanodeID());
            if (c != null && !c.isClosed()) {
                if (c.isRunning()) {
                    c.setHandler(null);
                    Logger.error("release running connection:{}", c);
                } else {
                    c.release();
                }
            }
        }
    }

    @Override
    public void clear() {
        this.getFront().reset();
        KeySetView<Integer, BackendConnection> keys = connections.keySet();
        for (Integer id : keys) {
            BackendConnection con = connections.remove(id);
            con.setHandler(null);
            con.close("cleared");
        }
    }

}
