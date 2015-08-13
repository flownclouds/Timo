package fm.liu.timo.manager.handler;

import java.util.ArrayList;
import java.util.HashMap;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.response.ResponseUtil;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.manager.response.ShowBackend;
import fm.liu.timo.manager.response.ShowConnection;
import fm.liu.timo.manager.response.ShowHeartbeat;
import fm.liu.timo.manager.response.ShowProcessor;
import fm.liu.timo.manager.response.ShowServer;
import fm.liu.timo.manager.response.ShowSession;
import fm.liu.timo.manager.response.ShowThread;
import fm.liu.timo.manager.response.ShowVersion;

public class DescHandler {
    private static final ArrayList<Head>       heads = new ArrayList<Head>();
    public static HashMap<String, ShowHandler> map   = new HashMap<>();

    static {
        heads.add(new Head("field"));
        heads.add(new Head("description"));
        map.put("server", new ShowServer());
        map.put("version", new ShowVersion());
        map.put("processor", new ShowProcessor());
        map.put("thread", new ShowThread());
        map.put("connection", new ShowConnection());
        map.put("session", new ShowSession());
        map.put("heartbeat", new ShowHeartbeat());
        map.put("latency", null);
        map.put("database", new ShowBackend());
        map.put("datanode", new ShowBackend());
        map.put("datasource", new ShowBackend());
        map.put("backend", new ShowBackend());
        map.put("command", new ShowBackend());
        map.put("operation", new ShowBackend());
        map.put("table", new ShowBackend());
        map.put("help", new ShowBackend());
    }

    public static void handle(String stmt, ManagerConnection c, int offset) {
        String table = stmt.substring(4).trim().toLowerCase();
        ShowHandler handler = map.get(table);
        if (handler != null) {
            ArrayList<Object[]> rows = new ArrayList<>();
            for (Head head : handler.getHeads()) {
                int i = 0;
                Object[] row = new Object[heads.size()];
                row[i++] = head.name;
                row[i++] = head.desc;
                rows.add(row);
            }
            ResponseUtil.write(c, heads, rows);
        } else {
            ResponseUtil.error(c);
        }
    }
}
