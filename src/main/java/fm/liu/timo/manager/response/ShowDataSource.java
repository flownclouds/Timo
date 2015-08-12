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
package fm.liu.timo.manager.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import fm.liu.timo.TimoConfig;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.Fields;
import fm.liu.timo.config.model.Datasource;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.mysql.PacketUtil;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.mysql.EOFPacket;
import fm.liu.timo.net.mysql.FieldPacket;
import fm.liu.timo.net.mysql.ResultSetHeaderPacket;
import fm.liu.timo.net.mysql.RowDataPacket;
import fm.liu.timo.util.IntegerUtil;
import fm.liu.timo.util.StringUtil;

/**
 * 查看数据源信息
 * 
 * @author xianmao.hexm 2010-9-26 下午04:56:26
 * @author wenfeng.cenwf 2011-4-25
 */
public final class ShowDataSource {

    private static final int                   FIELD_COUNT = 5;
    private static final ResultSetHeaderPacket header      = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[]         fields      = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket             eof         = new EOFPacket();

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

    public static void execute(ManagerConnection c, int name) {
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
        Map<Integer, Datasource> dataSources = conf.getDatasources();
        List<Integer> keys = new ArrayList<Integer>();
        if (name != 0) {
            Node dn = conf.getNodes().get(name);
            if (dn != null)
                for (Integer ds : dn.getSources().keySet()) {
                    keys.add(ds);
                }
        } else {
            keys.addAll(dataSources.keySet());
        }
        Collections.sort(keys);
        for (int key : keys) {
            RowDataPacket row = getRow(dataSources.get(key), c.getCharset());
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

    private static RowDataPacket getRow(Datasource dsc, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(dsc.getID() + "", charset));
        row.add(StringUtil.encode(dsc.getType().toString(), charset));
        row.add(StringUtil.encode(dsc.getHost(), charset));
        row.add(IntegerUtil.toBytes(dsc.getPort()));
        row.add(StringUtil.encode(dsc.getDB(), charset));
        return row;
    }

}
