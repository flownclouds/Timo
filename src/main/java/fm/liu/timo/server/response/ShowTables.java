package fm.liu.timo.server.response;

import java.util.ArrayList;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.manager.response.ResponseUtil;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.server.ServerConnection;

/**
 * @author liuhuanting
 */
public class ShowTables {

    public static void response(ServerConnection c) {
        Database database =
                TimoServer.getInstance().getConfig().getDatabases().get(c.getDB().toUpperCase());
        ArrayList<Head> heads = new ArrayList<>();
        heads.add(new Head("Tables_in_" + database.getName()));
        ArrayList<Object[]> rows = new ArrayList<>();
        for (String table : database.getTables().keySet()) {
            Object[] row = new Object[1];
            row[0] = table;
            rows.add(row);
        }
        ResponseUtil.write(c, heads, rows);
    }

}
