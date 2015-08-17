package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import fm.liu.timo.manager.handler.DescHandler;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;

/**
 * @author liuhuanting
 */
public class ShowHelp extends ShowHandler {
    private static final ArrayList<Head>     heads = new ArrayList<Head>();
    private static final Map<String, String> helps = new HashMap<>();

    static {
        heads.add(new Head("commands"));
        heads.add(new Head("comments"));
        for (Entry<String, ShowHandler> entry : DescHandler.map.entrySet()) {
            helps.put("show @@" + entry.getKey(),
                    entry.getValue() == null ? "unsupported yet" : entry.getValue().getInfo());
        }
        helps.put("stop @@heartbeat [datanode_id]:[time(s)]",
                "pause heartbeat for a while on the datanode you've chosen");
        helps.put("kill @@connection [connection_id]", "kill the connection you've chosen");
        helps.put("reload @@config", "reload the config online");
        helps.put("rollback @@config", "rollback the config to the early time");
        helps.put("online", "turn timo-server to online");
        helps.put("offline", "turn timo-server to offline");
        helps.put("handover @@datasource [datanode_id]",
                "handover datanode's datasource to the next");
    }

    @Override
    public String getInfo() {
        return "show commands in manager service of timo-server";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(helps.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            Object[] row = new Object[heads.size()];
            int i = 0;
            row[i++] = key;
            row[i++] = helps.get(key);
            rows.add(row);
        }
        return rows;
    }
}
