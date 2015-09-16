package fm.liu.timo.mysql.handler.xa;

import java.util.concurrent.atomic.AtomicInteger;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.XATransactionSession;
import fm.liu.timo.server.session.XATransactionSession.XAState;

/**
 * @author liuhuanting
 */
public class XAStartHandler extends XAHandler {

    public XAStartHandler(XATransactionSession session, int count) {
        super(session, null);
        super.count = new AtomicInteger(count);
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        if (decrement()) {
            if (failed()) {
                error();
                return;
            }
            TimoServer.getInstance().setXAStarting(false);
            session.setState(XAState.ACTIVE);
            OkPacket p = new OkPacket();
            p.read(ok);
            p.packetId = ++packetId;
            session.savepoint(p);
        }
    }

    private void error() {
        session.clear();
        TimoServer.getInstance().setXAStarting(false);
        session.getFront().writeErrMessage(ErrorCode.ER_YES, this.errMsg);
        recycleResources();
    }

    @Override
    public void error(byte[] error, BackendConnection con) {
        ErrorPacket err = new ErrorPacket();
        err.read(error);
        this.setFail(err.errno, new String(err.message));
        if (decrement()) {
            error();
        }
    }

}
