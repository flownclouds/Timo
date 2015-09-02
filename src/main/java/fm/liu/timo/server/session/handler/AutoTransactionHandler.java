package fm.liu.timo.server.session.handler;

import java.util.Collection;
import java.util.List;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.Session;

/**
 * @author liuhuanting
 */
public class AutoTransactionHandler extends SessionResultHandler {
    protected long affectedRows = 0;
    protected long insertId     = 0;

    public AutoTransactionHandler(Session session) {
        this.session = session;
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        session.release(con);
        OkPacket p = new OkPacket();
        p.read(ok);
        lock.lock();
        try {
            affectedRows += p.affectedRows;
            if (p.insertId > 0) {
                insertId = (insertId == 0) ? p.insertId : Math.min(insertId, p.insertId);
            }
        } finally {
            lock.unlock();
        }
        if (decrement()) {
            if (this.failed()) {
                session.clear();
                onError();
                return;
            }
            try {
                p.packetId = ++packetId;// OK_PACKET
                p.affectedRows = affectedRows;
                if (insertId > 0) {
                    p.insertId = insertId;
                    session.getFront().setLastInsertId(insertId);
                }
                prepareSendCommit(p);
            } catch (Exception e) {
                setFail(ErrorCode.ER_YES, e.toString());
                if (decrement()) {
                    onError();
                }
            }
        }
    }

    private void prepareSendCommit(OkPacket ok) {
        Collection<BackendConnection> cons = session.availableConnections();
        if (cons.size() == session.getConnections().size()) {
            commit(cons);
        } else {
            session.clear();
            session.getFront().writeErrMessage(ErrorCode.ER_YES,
                    "some connection already closed when commit");
        }
    }

    private void commit(Collection<BackendConnection> cons) {
        ResultHandler handler = new CommitHandler(session, cons);
        for (BackendConnection con : cons) {
            con.query("commit", handler);
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
