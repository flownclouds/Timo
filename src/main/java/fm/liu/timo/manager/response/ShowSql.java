package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.List;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.statistic.SQLRecord;
import fm.liu.timo.statistic.SQLRecorder;
import fm.liu.timo.util.FormatUtil;

/**
 * @author liuhuanting
 */
public class ShowSql extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<Head>();

    static {
        heads.add(new Head("datanode"));
        //        heads.add(new Head("datasource"));
        heads.add(new Head("host"));
        heads.add(new Head("db"));
        heads.add(new Head("start_time"));
        heads.add(new Head("execute_time"));
        heads.add(new Head("sql"));
        heads.add(new Head("count"));
    }

    @Override
    public String getInfo() {
        return "show the records of sqls in timo-server";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        SQLRecorder recorder = (SQLRecorder) TimoServer.getInstance().getRecorder();
        List<SQLRecord> recordes = recorder.getRecords();
        for (SQLRecord record : recordes) {
            if (record != null) {
                Object[] row = new Object[heads.size()];
                int i = 0;
                row[i++] = record.datanode;
                row[i++] = record.host;
                row[i++] = record.schema;
                row[i++] = FormatUtil.formatMillisTime(record.startTime);
                row[i++] = record.executeTime + "ms";
                row[i++] = record.statement;
                row[i++] = record.count.get();
                rows.add(row);
            }
        }
        return rows;
    }

}
