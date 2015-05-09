package re.ovo.timo.net.mysql;

import java.nio.ByteBuffer;

import re.ovo.timo.net.connection.FrontendConnection;

public abstract class MySQLServerPacket extends MySQLPacket {
	
	/*
	 * 写入packet时，要根据packet大小申请buffer，否则可能会导致buffer溢出
	 */
	public void write(FrontendConnection c) {
		int size = calcPacketSize();
		ByteBuffer buffer = c.allocate(size);
		write(buffer, size);
		c.write(buffer);
	}
}
