/*
 * Copyright (c) 2013, OpenCloudDB/HotDB and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package re.ovo.timo.net.mysql;

import java.nio.ByteBuffer;

import re.ovo.timo.mysql.BufferUtil;
import re.ovo.timo.mysql.MySQLMessage;

/**
 * @author hotdb
 */
public abstract class MySQLPacket {

	//
	public static final int PACKET_HEADER_SIZE = 4;// 固定值
	public static final int MAX_PACKET_SIZE = 16 * 1024 * 1024;// 固定值2^24,3个字节的最大值

	protected int packetLength;
	public byte packetId;


	/**
	 * 计算数据包大小，不包含包头长度。
	 */
	public abstract int calcPacketSize();

	/**
	 * 取得数据包信息
	 */
	protected abstract String getPacketInfo();

	@Override
	public String toString() {
		return new StringBuilder().append(getPacketInfo()).append("{length=")
				.append(packetLength).append(",id=").append(packetId)
				.append('}').toString();
	}

	/**
	 * 获得MySQL PacketLength
	 * @param buffer
	 * @param offset
	 * @return
	 */
	public static int getPacketLength(ByteBuffer buffer, int offset) {
		if (buffer.position() < offset + MySQLPacket.PACKET_HEADER_SIZE) {
			return -1;
		} else {
			int length = buffer.get(offset) & 0xff;
			length |= (buffer.get(++offset) & 0xff) << 8;
			length |= (buffer.get(++offset) & 0xff) << 16;
			return length + MySQLPacket.PACKET_HEADER_SIZE;
		}
	}
	
	public void read(byte[] data) {
		MySQLMessage mm = new MySQLMessage(data);
		packetLength = mm.readUB3();
		packetId = mm.read();
		readBody(mm);
	}
	protected abstract void readBody(MySQLMessage mm);
	/**
	 * 把mysql packet的内容写入到buffer中；
	 * 调用者已分配足够大的buffer空间,由调用者保证buffer.remaining() >= size，
	 * 不会出现一个packet分散写入多个buffer的情况，一个完整的mysql packet最多占用一个buffer。
	 * @param buffer
	 * @param size
	 */
	public void write(ByteBuffer buffer,int size) {
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		writeBody(buffer);
	}
	
	protected abstract void writeBody(ByteBuffer buffer);
}