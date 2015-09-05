package fm.liu.timo.server.session.handler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.Session;

/**
 * @author liuhuanting
 */
public class SavepointHandler extends SessionResultHandler {
    private OkPacket ok;

    public SavepointHandler(Session session, int count, OkPacket ok) {
        this.session = session;
        this.ok = ok;
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
            this.ok.write(session.getFront());
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
