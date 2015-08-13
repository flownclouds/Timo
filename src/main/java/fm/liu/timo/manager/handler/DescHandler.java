package fm.liu.timo.manager.handler;

import java.util.ArrayList;
import java.util.HashMap;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.response.ResponseUtil;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.manager.response.ShowBackend;

public class DescHandler {
    private static final ArrayList<Head>       heads = new ArrayList<Head>();
    public static HashMap<String, ShowHandler> map   = new HashMap<>();

    static {
        heads.add(new Head("field"));
        heads.add(new Head("description"));
        map.put("backend", new ShowBackend());
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
