/*
 * Copyright 2015 Liu Huanting.
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
package fm.liu.timo.net.mysql;

import java.nio.ByteBuffer;

import fm.liu.timo.mysql.BufferUtil;
import fm.liu.timo.mysql.MySQLMessage;

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