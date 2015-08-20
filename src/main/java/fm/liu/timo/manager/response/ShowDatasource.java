package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.Collection;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.model.Datasource;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.backend.Source;

/**
 * @author liuhuanting
 */
public class ShowDatasource extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<Head>();

    static {
        heads.add(new Head("node"));
        heads.add(new Head("source"));
        heads.add(new Head("source_id"));
        heads.add(new Head("type"));
        heads.add(new Head("idle_size", "active connection size of this source"));
        heads.add(new Head("total_size", "total connection size of this source"));
        heads.add(new Head("status"));
    }

    @Override
    public String getInfo() {
        return "show status of datasources on timo-server";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        Collection<Node> nodes = TimoServer.getInstance().getConfig().getNodes().values();
        for (Node node : nodes) {
            for (Source source : node.getSources().values()) {
                Object[] row = new Object[heads.size()];
                int i = 0;
                row[i++] = node.getID();
                Datasource config = source.getConfig();
                row[i++] = config.getHost() + ":" + config.getPort() + "/" + config.getDB();
                row[i++] = config.getID();
                row[i++] = config.getType();
                row[i++] = source.getIdleSize();
                row[i++] = source.getSize();
                row[i++] = source.getConfig().getStatus();
                rows.add(row);
            }
        }
        return rows;
    }
}
