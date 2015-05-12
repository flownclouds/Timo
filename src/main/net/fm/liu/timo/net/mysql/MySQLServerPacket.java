package fm.liu.timo.net.mysql;

import java.nio.ByteBuffer;

import fm.liu.timo.net.connection.FrontendConnection;

public abstract class MySQLServerPacket extends MySQLPacket {
	
	/*
	 * 写入packet时，要根据packet大小申请buffer，否则可能会导致buffer溢出
	 */
	public void write(FrontendConnection front) {
		int size = calcPacketSize();
		ByteBuffer buffer = front.allocate(size);
		write(buffer, size);
		front.write(buffer);
	}
}
