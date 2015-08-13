package fm.liu.timo.manager.response;

import java.util.ArrayList;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.util.NameableExecutor;

/**
 * @author liuhuanting
 */
public class ShowThread extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<>();

    static {
        heads.add(new Head("name"));
        heads.add(new Head("size"));
        heads.add(new Head("active_size"));
        heads.add(new Head("task_queue_size"));
        heads.add(new Head("completed_count"));
        heads.add(new Head("total_count"));
    }

    @Override
    public String getInfo() {
        return "show the status of thread pool";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TimoServer server = TimoServer.getInstance();
        for (NIOProcessor processor : server.getProcessors()) {
            NameableExecutor executor = (NameableExecutor) processor.getExecutor();
            if (executor != null) {
                Object[] row = new Object[heads.size()];
                int i = 0;
                row[i++] = executor.getName();
                row[i++] = executor.getPoolSize();
                row[i++] = executor.getActiveCount();
                row[i++] = executor.getQueue().size();
                row[i++] = executor.getCompletedTaskCount();
                row[i++] = executor.getTaskCount();
                rows.add(row);
            }
        }
        return rows;
    }

}
