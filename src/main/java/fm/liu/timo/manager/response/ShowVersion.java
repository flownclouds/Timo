package fm.liu.timo.manager.response;

import java.util.ArrayList;
import fm.liu.timo.config.Versions;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;

/**
 * @author liuhuanting
 */
public class ShowVersion extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<>();

    static {
        heads.add(new Head("version"));
    }

    @Override
    public String getInfo() {
        return "show the version of timo-server";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        Object[] row = new Object[heads.size()];
        int i = 0;
        row[i++] = new String(Versions.SERVER_VERSION);
        rows.add(row);
        return rows;
    }

}
