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
package re.ovo.timo.server.response;

import java.nio.ByteBuffer;
import java.util.Map;

import re.ovo.timo.TimoServer;
import re.ovo.timo.config.Fields;
import re.ovo.timo.config.model.DataSourceConfig;
import re.ovo.timo.mysql.MySQLDataNode;
import re.ovo.timo.mysql.MySQLDataSource;
import re.ovo.timo.mysql.PacketUtil;
import re.ovo.timo.net.mysql.EOFPacket;
import re.ovo.timo.net.mysql.FieldPacket;
import re.ovo.timo.net.mysql.ResultSetHeaderPacket;
import re.ovo.timo.net.mysql.RowDataPacket;
import re.ovo.timo.server.ServerConnection;
import re.ovo.timo.util.IntegerUtil;
import re.ovo.timo.util.StringUtil;

/**
 * 查询有效数据节点的当前数据源
 * 
 * @author xianmao.hexm
 */
public class ShowDataSources {

    private static final int FIELD_COUNT = 5;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("TYPE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("PORT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write field
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        Map<String, MySQLDataNode> nodes = TimoServer.getInstance().getConfig().getDataNodes();
        for (MySQLDataNode node : nodes.values()) {
            RowDataPacket row = getRow(node, c.getCharset());
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

    private static RowDataPacket getRow(MySQLDataNode node, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(node.getName(), charset));
        MySQLDataSource ds = node.getSource();
        if (ds != null) {
            DataSourceConfig dsc = ds.getConfig();
            row.add(StringUtil.encode(dsc.getType(), charset));
            row.add(StringUtil.encode(dsc.getHost(), charset));
            row.add(IntegerUtil.toBytes(dsc.getPort()));
            row.add(StringUtil.encode(dsc.getDatabase(), charset));
        } else {
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
        }
        return row;
    }

}
