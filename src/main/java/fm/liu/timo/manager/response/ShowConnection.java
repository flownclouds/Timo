package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.Collection;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.util.TimeUtil;

/**
 * @author liuhuanting
 */
public class ShowConnection extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<>();

    static {
        heads.add(new Head("processor"));
        heads.add(new Head("id"));
        heads.add(new Head("host"));
        heads.add(new Head("dest_port"));
        heads.add(new Head("db", "database"));
        heads.add(new Head("charset"));
        heads.add(new Head("up_time", "uptime(s)"));
        heads.add(new Head("received", "received bytes"));
        heads.add(new Head("sent", "sent bytes"));

    }

    @Override
    public String getInfo() {
        return "show the status of current connections";
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
            Collection<FrontendConnection> frontends = processor.getFrontends().values();
            for (FrontendConnection frontend : frontends) {
                Object[] row = new Object[heads.size()];
                int i = 0;
                row[i++] = processor.getName();
                row[i++] = frontend.getID();
                row[i++] = frontend.getHost() + ":" + frontend.getLocalPort();
                row[i++] = frontend.getPort();
                row[i++] = frontend.getDB();
                row[i++] = frontend.getCharset();
                row[i++] =
                        (TimeUtil.currentTimeMillis() - frontend.getVariables().getUpTime()) / 1000;
                row[i++] = frontend.getReadBuffer().capacity();
                row[i++] = frontend.getWriteQueue().size();
                rows.add(row);
            }
        }
        return rows;
    }

}
