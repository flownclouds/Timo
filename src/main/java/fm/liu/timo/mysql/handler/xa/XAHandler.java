package fm.liu.timo.mysql.handler.xa;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.XATransactionSession;
import fm.liu.timo.server.session.handler.SessionResultHandler;

/**
 * @author liuhuanting
 */
public abstract class XAHandler extends SessionResultHandler {
    protected XATransactionSession                                session;
    protected final ConcurrentHashMap<Integer, Boolean>           results =
            new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Integer, BackendConnection> cons    =
            new ConcurrentHashMap<>();

    public XAHandler(XATransactionSession session, Collection<BackendConnection> cons) {
        super.session = session;
        this.session = session;
        if (cons != null) {
            super.count = new AtomicInteger(cons.size());
            for (BackendConnection con : cons) {
                int id = con.getDatanodeID();
                results.put(id, false);
                this.cons.put(id, con);
            }
        }
    }

    @Override
    public void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con) {}

    @Override
    public void row(byte[] row, BackendConnection con) {}

    @Override
    public void eof(byte[] eof, BackendConnection con) {}

}
