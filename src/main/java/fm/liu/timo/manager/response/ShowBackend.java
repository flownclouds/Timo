package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.Collection;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.util.TimeUtil;

/**
 * 
 * @author liuhuanting
 *
 */
public class ShowBackend extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<>();

    static {
        heads.add(new Head("processor", "processor name"));
        heads.add(new Head("id"));
        heads.add(new Head("connect_id"));
        heads.add(new Head("dnid", "datanode id"));
        heads.add(new Head("host"));
        heads.add(new Head("db", "database"));
        heads.add(new Head("port"));
        heads.add(new Head("up_time", "uptime(s)"));
        heads.add(new Head("state", "connection state"));
        heads.add(new Head("send_queue"));
    }

    @Override
    public String getInfo() {
        return "show the status of backend connections";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        NIOProcessor[] processors = TimoServer.getInstance().getProcessors();
        for (NIOProcessor processor : processors) {
            Collection<BackendConnection> backends = processor.getBackends().values();
            for (BackendConnection backend : backends) {
                if (backend != null) {
                    Object[] row = new Object[heads.size()];
                    int i = 0;
                    row[i++] = processor.getName();
                    row[i++] = backend.getID();
                    row[i++] = ((MySQLConnection) backend).getThreadID();
                    row[i++] = backend.getDatanodeID();
                    row[i++] = backend.getHost() + ":" + backend.getPort();
                    row[i++] = ((MySQLConnection) backend).getDatasource().getConfig().getDB();
                    row[i++] = backend.getLocalPort();
                    row[i++] = (TimeUtil.currentTimeMillis() - backend.getVariables().getUpTime())
                            / 1000;
                    row[i++] = backend.getStateDesc();
                    row[i++] = ((MySQLConnection) backend).getWriteQueue().size();
                    rows.add(row);
                }
            }
        }
        return rows;
    }

}
