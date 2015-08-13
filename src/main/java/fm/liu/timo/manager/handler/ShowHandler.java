package fm.liu.timo.manager.handler;

import java.util.ArrayList;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.parser.ManagerParseShow;
import fm.liu.timo.manager.response.ResponseUtil;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.manager.response.ShowCollation;
import fm.liu.timo.manager.response.ShowVariables;

/**
 * 
 * @author liuhuanting
 *
 */
public abstract class ShowHandler {

    public abstract String getInfo();

    public abstract ArrayList<Head> getHeads();

    public abstract ArrayList<Object[]> getRows();

    public static void handle(String stmt, ManagerConnection c, int offset) {
        int rs = ManagerParseShow.parse(stmt, offset);
        switch (rs & 0xff) {
            case ManagerParseShow.COLLATION:
                new ShowCollation().execute(c);
                break;
            case ManagerParseShow.VARIABLES:
                new ShowVariables().execute(c);
                break;
            default:
                String table = stmt.substring(stmt.lastIndexOf('@') + 1).trim().toLowerCase();
                ShowHandler handler = DescHandler.map.get(table);
                if (handler != null) {
                    handler.execute(c);
                } else {
                    ResponseUtil.error(c);
                }
        }
    }

    public void execute(ManagerConnection c) {
        ResponseUtil.write(c, getHeads(), getRows());
    }
}
