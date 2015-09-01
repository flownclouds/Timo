package fm.liu.timo.manager.response;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.config.Fields;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.mysql.PacketUtil;
import fm.liu.timo.mysql.packet.EOFPacket;
import fm.liu.timo.mysql.packet.FieldPacket;
import fm.liu.timo.mysql.packet.ResultSetHeaderPacket;
import fm.liu.timo.mysql.packet.RowDataPacket;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.util.StringUtil;

public class ResponseUtil {
    public static class Head {
        public String name;
        public String desc;
        public int    type = Fields.FIELD_TYPE_VARCHAR;

        public Head(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        public Head(String name) {
            this.name = name;
            this.desc = name;
        }
    }

    public static void write(FrontendConnection c, ArrayList<Head> heads,
            ArrayList<Object[]> rows) {
        ByteBuffer buffer = c.allocate();
        int size = heads.size();
        ResultSetHeaderPacket header = PacketUtil.getHeader(size);
        byte packetId = 0;
        header.packetId = ++packetId;
        buffer = header.write(buffer, c);
        FieldPacket fielder = new FieldPacket();
        for (Head head : heads) {
            fielder = PacketUtil.getField(head.name, head.type);
            fielder.packetId = ++packetId;
            buffer = fielder.write(buffer, c);
        }
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, c);
        if (rows != null) {
            for (Object[] values : rows) {
                RowDataPacket row = new RowDataPacket(size);
                for (Object value : values) {
                    if (value == null) {
                        value = "NULL";
                    }
                    row.add(StringUtil.encode(value.toString(), c.getVariables().getCharset()));
                }
                row.packetId = ++packetId;
                buffer = row.write(buffer, c);
            }
        }
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, c);
        c.write(buffer);
    }

    public static void error(ManagerConnection c) {
        c.writeErrMessage(ErrorCode.ER_YES, "unsupported statement");
    }

    public static void error(ManagerConnection c, String reason) {
        c.writeErrMessage(ErrorCode.ER_YES, reason);
    }
}
