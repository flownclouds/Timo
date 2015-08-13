package fm.liu.timo.manager.response;

import java.util.ArrayList;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.net.NIOProcessor;

/**
 * @author liuhuanting
 */
public class ShowProcessor extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<>();

    static {
        heads.add(new Head("name"));
        heads.add(new Head("frontends", "the number of connections from frontends"));
        heads.add(new Head("backends", "the number of connections from backends"));
        heads.add(new Head("write_queue", "the size of write queue need to be sent"));
    }

    @Override
    public String getInfo() {
        return "show information about processors";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<Object[]>();
        for (NIOProcessor p : TimoServer.getInstance().getProcessors()) {
            Object[] row = new Object[heads.size()];
            int i = 0;
            row[i++] = p.getName();
            row[i++] = p.getFrontends().size();
            row[i++] = p.getBackends().size();
            row[i++] = p.getWriteQueueSize();
            rows.add(row);
        }
        return rows;
    }
}
