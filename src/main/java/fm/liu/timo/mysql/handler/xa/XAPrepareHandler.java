package fm.liu.timo.mysql.handler.xa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.XATransactionSession;
import fm.liu.timo.server.session.XATransactionSession.XAState;

/**
 * @author liuhuanting
 */
public class XAPrepareHandler extends XAHandler {
    public XAPrepareHandler(XATransactionSession session, Collection<BackendConnection> cons) {
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
            session.setState(XAState.PREPARED);
            log();
            session.xaCommit();
        }

    }

    private void log() {
        File file = new File(session.getXID().replace("'", ""));
        try {
            FileOutputStream out = new FileOutputStream(file);
            ObjectOutputStream stream = new ObjectOutputStream(out);
            stream.writeObject(results);
            stream.flush();
            out.getFD().sync();
            stream.close();
            out.close();
            session.setPrepareLog(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void error() {
        Collection<BackendConnection> connections = new HashSet<>();
        results.entrySet().stream().filter(entry -> entry.getValue())
                .forEach(i -> connections.add(cons.get(i)));
        XARollbackHandler handler = new XARollbackHandler(session, connections, false);
        connections.forEach(con -> con.query("XA ROLLBACK " + session.getXID(), handler));
        session.getFront().writeErrMessage(ErrorCode.ER_YES, this.errMsg);
        session.getFront().reset();
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
