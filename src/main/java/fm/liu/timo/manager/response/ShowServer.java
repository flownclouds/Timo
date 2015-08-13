package fm.liu.timo.manager.response;

import java.lang.reflect.Field;
import java.util.ArrayList;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.util.FormatUtil;
import fm.liu.timo.util.TimeUtil;

public class ShowServer extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<>();
    private ManagerConnection            c;

    static {
        heads.add(new Head("uptime"));
        heads.add(new Head("used_memory"));
        heads.add(new Head("total_memory"));
        heads.add(new Head("max_memory"));
        heads.add(new Head("max_direct_memory"));
        heads.add(new Head("used_direct_memory"));
        heads.add(new Head("charset"));
        heads.add(new Head("status"));
    }

    @Override
    public String getInfo() {
        return "show the status of timo-server";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        Object[] row = new Object[heads.size()];
        TimoServer server = TimoServer.getInstance();
        Runtime rt = Runtime.getRuntime();
        try {
            Class<?> mem = Class.forName("java.nio.Bits");
            Field maxMemory = mem.getDeclaredField("maxMemory");
            maxMemory.setAccessible(true);
            Field reservedMemory = mem.getDeclaredField("reservedMemory");
            reservedMemory.setAccessible(true);
            Long maxMemoryValue = (Long) maxMemory.get(null);
            Long reservedMemoryValue = (Long) reservedMemory.get(null);
            int i = 0;
            long m = 1024 * 1024;
            row[i++] = FormatUtil.formatTime(TimeUtil.currentTimeMillis() - server.getStartupTime(),
                    3);
            row[i++] = String.valueOf((rt.totalMemory() - rt.freeMemory()) / m) + "M";
            row[i++] = String.valueOf(rt.totalMemory() / m) + "M";
            row[i++] = String.valueOf(rt.maxMemory() / m) + "M";
            row[i++] = String.valueOf(maxMemoryValue / m) + "M";
            row[i++] = String.valueOf(reservedMemoryValue / m) + "M";
            if (c == null) {
                row[i++] = "NULL";
            } else {
                row[i++] = c.getCharset();
            }
            row[i++] = server.isOnline() ? "ON" : "OFF";
            rows.add(row);
        } catch (Exception e) {
            ResponseUtil.error(c, e.toString());
        }
        return rows;
    }

    @Override
    public void execute(ManagerConnection c) {
        this.c = c;
        ResponseUtil.write(c, heads, getRows());
    }

}
