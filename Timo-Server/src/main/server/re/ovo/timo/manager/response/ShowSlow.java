/*
 * Copyright 1999-2012 Alibaba Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package re.ovo.timo.manager.response;

import java.nio.ByteBuffer;
import java.util.Map;

import re.ovo.timo.TimoConfig;
import re.ovo.timo.TimoServer;
import re.ovo.timo.config.Fields;
import re.ovo.timo.config.model.SchemaConfig;
import re.ovo.timo.manager.ManagerConnection;
import re.ovo.timo.mysql.MySQLDataNode;
import re.ovo.timo.mysql.MySQLDataSource;
import re.ovo.timo.mysql.PacketUtil;
import re.ovo.timo.net.mysql.EOFPacket;
import re.ovo.timo.net.mysql.FieldPacket;
import re.ovo.timo.net.mysql.ResultSetHeaderPacket;
import re.ovo.timo.net.mysql.RowDataPacket;
import re.ovo.timo.statistic.SQLRecord;
import re.ovo.timo.statistic.SQLRecorder;
import re.ovo.timo.util.IntegerUtil;
import re.ovo.timo.util.LongUtil;
import re.ovo.timo.util.StringUtil;

/**
 * 取得执行时间最长的SQL集
 * 
 * @author xianmao.hexm
 */
public final class ShowSlow {

    private static final int FIELD_COUNT = 7;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("DATASOURCE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("INDEX", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("START_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("EXECUTE_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void dataNode(ManagerConnection c, String name) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        TimoConfig conf = TimoServer.getInstance().getConfig();
        MySQLDataNode dn = conf.getDataNodes().get(name);
        MySQLDataSource ds = null;
        if (dn != null && (ds = dn.getSource()) != null) {
            SQLRecord[] records = ds.getSqlRecorder().getRecords();
            for (int i = records.length - 1; i >= 0; i--) {
                if (records[i] != null) {
                    RowDataPacket row = getRow(records[i], c.getCharset());
                    row.packetId = ++packetId;
                    buffer = row.write(buffer, c);
                }
            }
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // write buffer
        c.write(buffer);
    }

    public static void schema(ManagerConnection c, String name) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        TimoConfig conf = TimoServer.getInstance().getConfig();
        SchemaConfig schema = conf.getSchemas().get(name);
        if (schema != null) {
            SQLRecorder recorder = new SQLRecorder(conf.getSystem().getSqlRecordCount());
            Map<String, MySQLDataNode> dataNodes = conf.getDataNodes();
            for (String n : schema.getAllDataNodes()) {
                MySQLDataNode dn = dataNodes.get(n);
                MySQLDataSource ds = null;
                if (dn != null && (ds = dn.getSource()) != null) {
                    for (SQLRecord r : ds.getSqlRecorder().getRecords()) {
                        if (r != null && recorder.check(r.executeTime)) {
                            recorder.add(r);
                        }
                    }
                }
            }
            SQLRecord[] records = recorder.getRecords();
            for (int i = records.length - 1; i >= 0; i--) {
                if (records[i] != null) {
                    RowDataPacket row = getRow(records[i], c.getCharset());
                    row.packetId = ++packetId;
                    buffer = row.write(buffer, c);
                }
            }
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // write buffer
        c.write(buffer);
    }

    private static RowDataPacket getRow(SQLRecord sqlR, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(sqlR.host, charset));
        row.add(StringUtil.encode(sqlR.schema, charset));
        row.add(StringUtil.encode(sqlR.dataNode, charset));
        row.add(IntegerUtil.toBytes(sqlR.dataNodeIndex));
        row.add(LongUtil.toBytes(sqlR.startTime));
        row.add(LongUtil.toBytes(sqlR.executeTime));
        row.add(StringUtil.encode(sqlR.statement, charset));
        return row;
    }

}
