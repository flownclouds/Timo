package fm.liu.timo.server.session.handler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.Session;

/**
 * @author liuhuanting
 */
public class RollbackToSavepointHandler extends SessionResultHandler {
    private ErrorPacket err;

    public RollbackToSavepointHandler(Session session, int count, ErrorPacket err) {
        this.session = session;
        this.err = err;
        this.count = new AtomicInteger(count);
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        session.release(con);
        if (decrement()) {
            if (this.failed()) {
                session.clear();
                onError();
                return;
            }
            this.err.write(session.getFront());
        }
    }

    @Override
    public void error(byte[] error, BackendConnection con) {
        session.clear();
        session.getFront().write(error);
    }

    @Override
    public void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con) {}

    @Override
    public void row(byte[] row, BackendConnection con) {}

    @Override
    public void eof(byte[] eof, BackendConnection con) {}

}
