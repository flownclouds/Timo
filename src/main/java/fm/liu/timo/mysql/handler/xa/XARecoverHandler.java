package fm.liu.timo.mysql.handler.xa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.pmw.tinylog.Logger;
import fm.liu.messenger.Mail;
import fm.liu.timo.TimoServer;
import fm.liu.timo.backend.Node;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.mysql.packet.CommandPacket;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.mysql.packet.RowDataPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.handler.SessionResultHandler;

/**
 * @author liuhuanting
 */
public class XARecoverHandler extends SessionResultHandler {
    private Map<Integer, Node>                                   nodes;
    private HashMap<String, ConcurrentHashMap<Integer, Boolean>> logResult;
    private volatile HashMap<String, HashSet<Integer>>           dbResult;
    private volatile AtomicInteger                               recoverCount;

    public XARecoverHandler(HashMap<String, ConcurrentHashMap<Integer, Boolean>> recoveryLog,
            Map<Integer, Node> nodes) {
        super.count = new AtomicInteger(nodes.size());
        this.nodes = nodes;
        this.logResult = recoveryLog;
        this.dbResult = new HashMap<>();
    }

    public XARecoverHandler(int size) {
        super.count = new AtomicInteger(size);
    }

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        if (decrement()) {
            if (this.failed()) {
                error();
            }
            TimoServer.getSender()
                    .send(new Mail<String>(TimoServer.getInstance().getStarter(), "start"));
        }
    }

    @Override
    public void error(byte[] error, BackendConnection con) {
        ErrorPacket err = new ErrorPacket();
        err.read(error);
        String message = new String(err.message);
        Logger.warn("error :{} received from :{} when RECOVER", con, message);
        this.setFail(err.errno, message);
        if (decrement()) {
            error();
        }
    }

    private void error() {
        System.exit(-1);
    }

    @Override
    public void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con) {}

    @Override
    public void row(byte[] row, BackendConnection con) {
        RowDataPacket packet = new RowDataPacket(4);
        packet.read(row);
        String data = new String(packet.fieldValues.get(3));
        this.recoverCount.incrementAndGet();
        if (dbResult.containsKey(data)) {
            dbResult.get(data).add(con.getDatanodeID());
        } else {
            HashSet<Integer> set = new HashSet<>();
            set.add(con.getDatanodeID());
            dbResult.put(data, set);
        }
    }

    @Override
    public void eof(byte[] eof, BackendConnection con) {
        con.release();
        if (decrement()) {
            if (this.failed()) {
                error();
            } else {
                if (this.recoverCount == null) {
                    TimoServer.getSender()
                            .send(new Mail<String>(TimoServer.getInstance().getStarter(), "start"));
                } else {
                    XARecoverHandler handler = new XARecoverHandler(this.recoverCount.get());
                    for (String xid : dbResult.keySet()) {
                        HashSet<Integer> recover = dbResult.get(xid);
                        if (logResult.containsKey(xid)) {
                            recover.parallelStream().forEach(i -> {
                                MySQLConnection conn =
                                        (MySQLConnection) nodes.get(i).getSource().notNullGet();
                                conn.setResultHandler(handler);
                                CommandPacket packet = new CommandPacket(CommandPacket.COM_QUERY);
                                packet.arg = ("XA COMMIT '" + xid + "'").getBytes();
                                packet.write(conn);
                            });
                        } else {
                            recover.parallelStream().forEach(i -> {
                                MySQLConnection conn =
                                        (MySQLConnection) nodes.get(i).getSource().notNullGet();
                                conn.setResultHandler(handler);
                                CommandPacket packet = new CommandPacket(CommandPacket.COM_QUERY);
                                packet.arg = ("XA ROLLBACK '" + xid + "'").getBytes();
                                packet.write(conn);
                            });
                        }
                    }
                }
            }
        }
    }

}
