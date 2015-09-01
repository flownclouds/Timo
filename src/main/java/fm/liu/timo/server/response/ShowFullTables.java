package fm.liu.timo.server.response;

import java.util.ArrayList;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.manager.response.ResponseUtil;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.server.ServerConnection;

/**
 * @author liuhuanting
 */
public class ShowFullTables {
    public static void response(ServerConnection c) {
        if (c.getDB() == null) {
            c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return;
        }
        Database database =
                TimoServer.getInstance().getConfig().getDatabases().get(c.getDB().toUpperCase());
        ArrayList<Head> heads = new ArrayList<>();
        heads.add(new Head("Tables_in_" + database.getName()));
        heads.add(new Head("Table_type"));
        ArrayList<Object[]> rows = new ArrayList<>();
        for (String table : database.getTables().keySet()) {
            Object[] row = new Object[2];
            row[0] = table;
            row[1] = "BASE TABLE";
            rows.add(row);
        }
        ResponseUtil.write(c, heads, rows);
    }
}
