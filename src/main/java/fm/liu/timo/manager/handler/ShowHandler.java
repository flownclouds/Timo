package fm.liu.timo.manager.handler;

import java.util.ArrayList;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.response.ResponseUtil;
import fm.liu.timo.manager.response.ResponseUtil.Head;

/**
 * 
 * @author liuhuanting
 *
 */
public abstract class ShowHandler {

    public abstract String getInfo();

    public abstract ArrayList<Head> getHeads();

    public abstract ArrayList<Object[]> getRows();

    public static void handle(String stmt, ManagerConnection c) {
        String table = stmt.substring(stmt.lastIndexOf('@') + 1).trim().toLowerCase();
        ShowHandler handler = DescHandler.map.get(table);
        if (handler != null) {
            handler.execute(c);
        } else {
            ResponseUtil.error(c);
        }
    }

    public void execute(ManagerConnection c) {
        ResponseUtil.write(c, getHeads(), getRows());
    }
}
