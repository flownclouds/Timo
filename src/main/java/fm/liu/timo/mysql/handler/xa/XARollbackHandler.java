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
public class XARollbackHandler extends XAHandler {

    private boolean response;

    public XARollbackHandler(XATransactionSession session, Collection<BackendConnection> cons,
            boolean response) {
        super(session, cons);
        this.response = response;
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        results.replace(con.getDatanodeID(), true);
        if (decrement()) {
            if (failed()) {
                error();
                return;
            }
            session.release();
            if (response) {
                session.getFront().write(OkPacket.OK);
            }
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
        XARollbackHandler handler = new XARollbackHandler(session, cons, true);
        connections.forEach(con -> con.query("XA ROLLBACK " + session.getXID(), handler));
        recycleResources();

    }

    @Override
    public void error(byte[] error, BackendConnection con) {
        ErrorPacket err = new ErrorPacket();
        err.read(error);
        String message = new String(err.message);
        Logger.warn("error :{} received from :{} when XA PREPARE", con, message);
        this.setFail(err.errno, message);
        if (decrement()) {
            error();
        }

    }
}
