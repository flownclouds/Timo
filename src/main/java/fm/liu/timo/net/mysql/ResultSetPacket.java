package fm.liu.timo.net.mysql;

import java.nio.ByteBuffer;

import fm.liu.timo.net.connection.FrontendConnection;

public abstract class ResultSetPacket extends MySQLServerPacket {

    /**
     * 对于结果集包，禁止直接调用不关联buffer的write方法
     */
    public void write(FrontendConnection c) {
        throw new RuntimeException("ResultSetPacket not support write(FrontendConnection c)");
    }

    /**
     * 把数据包写到buffer中，如果buffer满了就把buffer通过前端连接写出 (writeSocketIfFull=true)。
     */
    public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
        int size = calcPacketSize();
        buffer = c.checkWriteBuffer(buffer, MySQLPacket.PACKET_HEADER_SIZE + size);
        write(buffer, size);
        return buffer;
    }
}
