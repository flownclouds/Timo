package fm.liu.timo.manager.response;

import java.util.ArrayList;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;

/**
 * @author liuhuanting
 */
public class ShowCollation extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<Head>();

    private static ArrayList<Object[]> rows = new ArrayList<Object[]>();

    static {
        heads.add(new Head("collation"));
        heads.add(new Head("charset"));
        heads.add(new Head("id"));
        heads.add(new Head("default"));
        heads.add(new Head("compiled"));
        heads.add(new Head("sortlen"));
        String[] row = new String[heads.size()];
        int i = 0;
        row[i++] = "utf8_general_ci";
        row[i++] = "utf8";
        row[i++] = "33";
        row[i++] = "Yes";
        row[i++] = "Yes";
        row[i++] = "1";
        rows.add(row);
    }

    @Override
    public String getInfo() {
        return "show collation";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        return rows;
    }

}
