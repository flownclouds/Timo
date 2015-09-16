package fm.liu.timo.mysql.handler.xa;

import java.util.Collection;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.XATransactionSession;
import fm.liu.timo.server.session.XATransactionSession.XAState;

/**
 * @author liuhuanting
 */
public class XAEndHandler extends XAHandler {

    private boolean isCommit;

    public XAEndHandler(XATransactionSession session, Collection<BackendConnection> cons,
            boolean isCommit) {
        super(session, cons);
        this.isCommit = isCommit;
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        if (decrement()) {
            if (failed()) {
                error();
                return;
            }
            session.setState(XAState.IDLE);
            if (isCommit) {
                session.xaPrepare();
            } else {
                session.xaRollback();
            }
        }
    }

    @Override
    public void error(byte[] error, BackendConnection con) {
        ErrorPacket err = new ErrorPacket();
        err.read(error);
        String message = new String(err.message);
        Logger.warn("error :{} received from :{} when XA END", con, message);
        this.setFail(err.errno, message);
        if (decrement()) {
            error();
        }
    }

    private void error() {
        session.clear();
        session.getFront().writeErrMessage(ErrorCode.ER_YES, this.errMsg);
        recycleResources();
    }
}
