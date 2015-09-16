package fm.liu.timo.mysql.handler.xa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.pmw.tinylog.Logger;
import fm.liu.timo.TimoServer;
import fm.liu.timo.backend.Node;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.XATransactionSession;

/**
 * @author liuhuanting
 */
public class XACommitHandler extends XAHandler {

    public XACommitHandler(XATransactionSession session, Collection<BackendConnection> cons) {
        super(session, cons);
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        results.replace(con.getDatanodeID(), true);
        if (decrement()) {
            if (failed()) {
                error();
                return;
            }
            TimoServer.getInstance().setXACommiting(false);
            session.release();
            session.getFront().write(OkPacket.OK);
            recycleResources();
        }
    }

    private void error() {
        Collection<BackendConnection> connections = new HashSet<>();
        results.entrySet().stream().filter(entry -> !entry.getValue())
                .forEach(i -> connections.add(cons.get(i)));
        Map<Integer, Node> nodes = TimoServer.getInstance().getConfig().getNodes();
        Collection<BackendConnection> cons = new HashSet<>();
        for (BackendConnection con : connections) {
            if (con.isClosed()) {
                cons.add(nodes.get(con.getDatanodeID()).getSource().notNullGet());
            } else {
                cons.add(con);
            }
        }
        XACommitHandler handler = new XACommitHandler(session, cons);
        connections.forEach(con -> con.query("XA COMMIT " + session.getXID(), handler));
        recycleResources();
    }

    @Override
    public void error(byte[] error, BackendConnection con) {
        ErrorPacket err = new ErrorPacket();
        err.read(error);
        String message = new String(err.message);
        Logger.warn("error :{} received from :{} when XA COMMIT", con, message);
        this.setFail(err.errno, message);
        if (decrement()) {
            error();
        }
    }
}
