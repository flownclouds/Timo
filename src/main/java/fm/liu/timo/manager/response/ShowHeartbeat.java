package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.Map;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.model.Datasource;
import fm.liu.timo.heartbeat.Heartbeat;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.backend.Source;
import fm.liu.timo.util.FormatUtil;

/**
 * @author liuhuanting
 */
public class ShowHeartbeat extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<Head>();

    static {
        heads.add(new Head("node"));
        heads.add(new Head("source"));
        heads.add(new Head("source_type"));
        heads.add(new Head("host"));
        heads.add(new Head("db"));
        heads.add(new Head("retry"));
        heads.add(new Head("status"));
        heads.add(new Head("last_active_time"));
        heads.add(new Head("stoped"));
    }

    @Override
    public String getInfo() {
        return "show the status of heartbeat check";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        Map<Integer, Node> nodes = TimoServer.getInstance().getConfig().getNodes();
        for (Node node : nodes.values()) {
            for (Source source : node.getSources().values()) {
                Datasource config = source.getConfig();
                Heartbeat heartbeat = source.getHeartbeat();
                Object[] row = new Object[heads.size()];
                int i = 0;
                row[i++] = source.getDatanodeID();
                row[i++] = config.getID();
                row[i++] = config.getType();
                row[i++] = config.getHost() + ":" + config.getPort();
                row[i++] = config.getDB();
                row[i++] = heartbeat.getErrorCount();
                row[i++] = heartbeat.getStatus();
                row[i++] = FormatUtil.formatTime(heartbeat.getLastActiveTime());
                row[i++] = heartbeat.isStoped() ? "true" : "false";
                rows.add(row);
            }
        }
        return rows;
    }

}
