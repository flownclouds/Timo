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
/**
 * (created at 2011-11-25)
 */
package fm.liu.timo.server.handler;

import java.nio.ByteBuffer;
import java.sql.SQLNonTransientException;

import org.pmw.tinylog.Logger;

import fm.liu.timo.TimoServer;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.config.Fields;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.mysql.PacketUtil;
import fm.liu.timo.net.mysql.EOFPacket;
import fm.liu.timo.net.mysql.FieldPacket;
import fm.liu.timo.net.mysql.ResultSetHeaderPacket;
import fm.liu.timo.net.mysql.RowDataPacket;
import fm.liu.timo.route.Outlet;
import fm.liu.timo.route.Outlets;
import fm.liu.timo.route.Router;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.server.parser.ServerParse;
import fm.liu.timo.util.StringUtil;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class ExplainHandler {

    private static final int FIELD_COUNT = 2;
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    static {
        fields[0] = PacketUtil.getField("DATA_NODE", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
    }

    public static void handle(String stmt, ServerConnection c, int offset) {
        stmt = stmt.substring(offset);

        
        Outlets outltes = getRouteResultset(c, stmt);
        if (outltes == null)
            return;

        ByteBuffer buffer = c.allocate();

        // write header
        ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
        byte packetId = header.packetId;
        buffer = header.write(buffer, c);

        // write fields
        for (FieldPacket field : fields) {
            field.packetId = ++packetId;
            buffer = field.write(buffer, c);
        }

        // write eof
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, c);

        // write rows
        for (Outlet outlet : outltes.getResult()) {
            RowDataPacket row = getRow(outlet, c.getCharset());
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

    private static RowDataPacket getRow(Outlet node, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(node.getID()+"", charset));
        row.add(StringUtil.encode(node.getSql(), charset));
        return row;
    }

    private static Outlets getRouteResultset(ServerConnection c, String stmt) {
        String db = c.getDB();
        if (db == null) {
            c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return null;
        }
        Database database = TimoServer.getInstance().getConfig().getDatabases().get(db.toUpperCase());
        if (database == null) {
            c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return null;
        }
        try {
            int sqlType = ServerParse.parse(stmt) & 0xff;
            return Router.route(database, stmt, c.getCharset(), sqlType);
        } catch (SQLNonTransientException e) {
            StringBuilder s = new StringBuilder();
            Logger.warn(s.append(c).append(stmt).toString(), e);
            String msg = e.getMessage();
            c.writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.getClass().getSimpleName()
                    : msg);
            return null;
        }
    }

}
