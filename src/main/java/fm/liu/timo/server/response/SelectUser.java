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
package fm.liu.timo.server.response;

import java.nio.ByteBuffer;

import fm.liu.timo.TimoServer;
import fm.liu.timo.config.Fields;
import fm.liu.timo.mysql.PacketUtil;
import fm.liu.timo.net.mysql.EOFPacket;
import fm.liu.timo.net.mysql.ErrorPacket;
import fm.liu.timo.net.mysql.FieldPacket;
import fm.liu.timo.net.mysql.ResultSetHeaderPacket;
import fm.liu.timo.net.mysql.RowDataPacket;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.util.StringUtil;

/**
 * @author xianmao.hexm
 */
public class SelectUser {

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    private static final ErrorPacket error = PacketUtil.getShutdown();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("USER()", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        if (TimoServer.getInstance().isOnline()) {
            ByteBuffer buffer = c.allocate();
            buffer = header.write(buffer, c);
            for (FieldPacket field : fields) {
                buffer = field.write(buffer, c);
            }
            buffer = eof.write(buffer, c);
            byte packetId = eof.packetId;
            RowDataPacket row = new RowDataPacket(FIELD_COUNT);
            row.add(getUser(c));
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
            EOFPacket lastEof = new EOFPacket();
            lastEof.packetId = ++packetId;
            buffer = lastEof.write(buffer, c);
            c.write(buffer);
        } else {
            error.write(c);
        }
    }

    private static byte[] getUser(ServerConnection c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getUser()).append('@').append(c.getHost());
        return StringUtil.encode(sb.toString(), c.getCharset());
    }

}
