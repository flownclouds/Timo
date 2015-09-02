package fm.liu.timo.server.session.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.Session;

/**
 * @author liuhuanting
 */
public class CommitHandler extends SessionResultHandler {
    private final ConcurrentHashMap<Integer, Boolean> results = new ConcurrentHashMap<>();

    public CommitHandler(Session session, Collection<BackendConnection> cons) {
        this.session = session;
        this.count = new AtomicInteger(cons.size());
        for (BackendConnection con : cons) {
            results.put(con.getDatanodeID(), false);
        }
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        results.replace(con.getDatanodeID(), true);
        if (decrement()) {
            if (failed()) {
                error();
                return;
            }
            session.clear();
            session.getFront().write(ok);
            recycleResources();
        }
    }

    private void error() {
        session.clear();
        StringBuilder success = new StringBuilder();
        StringBuilder failed = new StringBuilder();
        for (Entry<Integer, Boolean> entry : results.entrySet()) {
            if (entry.getValue()) {
                if (success.length() > 0) {
                    success.append(",");
                }
                success.append(entry.getKey());
            } else {
                if (failed.length() > 0) {
                    failed.append(",");
                }
                failed.append(entry.getKey());
            }
        }
        StringBuilder msg = new StringBuilder("transaction error. success nodes: {");
        msg.append(success.toString()).append("}, failed nodes :{").append(failed.toString())
                .append(". error message is :").append(this.errMsg);
        session.getFront().writeErrMessage(ErrorCode.ER_YES, msg.toString());
        recycleResources();
    }

    @Override
    public void error(byte[] error, BackendConnection con) {
        ErrorPacket err = new ErrorPacket();
        err.read(error);
        String message = new String(err.message);
        Logger.warn("error :{} received from :{} when commit", con, message);
        this.setFail(err.errno, message);
        session.release(con);
        if (decrement()) {
            error();
        }
    }

    @Override
    public void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con) {}

    @Override
    public void row(byte[] row, BackendConnection con) {}

    @Override
    public void eof(byte[] eof, BackendConnection con) {}

    @Override
    public void close(String reason) {
        this.setFail(ErrorCode.ER_YES, reason);
        if (decrement()) {
            error();
        }
    }

    @Override
    public void setSQL(String sql) {}
}
