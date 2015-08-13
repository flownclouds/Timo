package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.Set;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;

/**
 * @author liuhuanting
 */
public class ShowDatabase extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<Head>();

    static {
        heads.add(new Head("database"));
    }

    @Override
    public String getInfo() {
        return "show the logic database in timo-server";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        Set<String> databases = TimoServer.getInstance().getConfig().getDatabases().keySet();
        Object[] row = new Object[heads.size()];
        int i = 0;
        for (String database : databases) {
            row[i++] = database;
        }
        rows.add(row);
        return rows;
    }

}
