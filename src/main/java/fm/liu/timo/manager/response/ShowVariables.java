package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;

/**
 * @author liuhuanting
 */
public class ShowVariables extends ShowHandler {
    private static final ArrayList<Head>     heads     = new ArrayList<Head>();
    private static final Map<String, String> variables = new HashMap<String, String>();

    static {
        heads.add(new Head("variable_name"));
        heads.add(new Head("value"));
        variables.put("character_set_client", "utf8");
        variables.put("character_set_connection", "utf8");
        variables.put("character_set_results", "utf8");
        variables.put("character_set_server", "utf8");
        variables.put("init_connect", "");
        variables.put("interactive_timeout", "172800");
        variables.put("lower_case_table_names", "1");
        variables.put("max_allowed_packet", "16777216");
        variables.put("net_buffer_length", "8192");
        variables.put("net_write_timeout", "60");
        variables.put("query_cache_size", "0");
        variables.put("query_cache_type", "OFF");
        variables.put("sql_mode", "STRICT_TRANS_TABLES");
        variables.put("system_time_zone", "CST");
        variables.put("time_zone", "SYSTEM");
        variables.put("lower_case_table_names", "1");
        variables.put("tx_isolation", "REPEATABLE-READ");
        variables.put("wait_timeout", "172800");
    }

    @Override
    public String getInfo() {
        return "show variables";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        for (Entry<String, String> entry : variables.entrySet()) {
            Object[] row = new Object[heads.size()];
            int i = 0;
            row[i++] = entry.getKey();
            row[i++] = entry.getValue();
            rows.add(row);
        }
        return rows;
    }

}
