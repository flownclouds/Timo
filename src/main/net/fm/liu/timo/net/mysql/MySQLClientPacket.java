package fm.liu.timo.net.mysql;

import java.nio.ByteBuffer;

import fm.liu.timo.net.connection.AbstractConnection;

public abstract class MySQLClientPacket extends MySQLPacket {

	/**
	 * 把数据包通过后端连接写出，一般使用buffer机制来提高写的吞吐量。
	 */
	public void write(AbstractConnection c) {
		int size = calcPacketSize();
		ByteBuffer buffer = c.allocate(size + MySQLPacket.PACKET_HEADER_SIZE);
		write(buffer, size);
		c.write(buffer);
	}
}
