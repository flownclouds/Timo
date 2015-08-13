package fm.liu.timo.manager.response;

import java.util.ArrayList;
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
        for (Entry<String, String> entry : helps.entrySet()) {
            Object[] row = new Object[heads.size()];
            int i = 0;
            row[i++] = entry.getKey();
            row[i++] = entry.getValue();
            rows.add(row);
        }
        return rows;
    }
}
