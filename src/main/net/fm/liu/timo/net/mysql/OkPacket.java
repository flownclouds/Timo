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
package fm.liu.timo.net.mysql;

import java.nio.ByteBuffer;

import fm.liu.timo.mysql.BufferUtil;
import fm.liu.timo.mysql.MySQLMessage;

/**
 * From server to client in response to command, if no error and no result set.
 * 
 * <pre>
 * Bytes                       Name
 * -----                       ----
 * 1                           field_count, always = 0
 * 1-9 (Length Coded Binary)   affected_rows
 * 1-9 (Length Coded Binary)   insert_id
 * 2                           server_status
 * 2                           warning_count
 * n   (until end of packet)   message fix:(Length Coded String)
 * 
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#OK_Packet
 * </pre>
 * 
 * @author hotdb
 */
public class OkPacket extends MySQLServerPacket {
	public static final byte FIELD_COUNT = 0x00;
	public static final byte[] OK = new byte[] { 7, 0, 0, 1, 0, 0, 0, 2, 0, 0,
			0 };

	public byte fieldCount = FIELD_COUNT;
	public long affectedRows;
	public long insertId;
	public int serverStatus;
	public int warningCount;
	public byte[] message;

	@Override
	protected void readBody(MySQLMessage mm){
		fieldCount = mm.read();
		affectedRows = mm.readLength();
		insertId = mm.readLength();
		serverStatus = mm.readUB2();
		warningCount = mm.readUB2();
		if (mm.hasRemaining()) {
			this.message = mm.readBytesWithLength();
		}
	}
	
	@Override
	protected void writeBody(ByteBuffer buffer) {
		buffer.put(fieldCount);
		BufferUtil.writeLength(buffer, affectedRows);
		BufferUtil.writeLength(buffer, insertId);
		BufferUtil.writeUB2(buffer, serverStatus);
		BufferUtil.writeUB2(buffer, warningCount);
		if (message != null) {
			BufferUtil.writeWithLength(buffer, message);
		}
	}
	
	@Override
	public int calcPacketSize() {
		int i = 1;
		i += BufferUtil.getLength(affectedRows);
		i += BufferUtil.getLength(insertId);
		i += 4;
		if (message != null) {
			i += BufferUtil.getLength(message);
		}
		return i;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL OK Packet";
	}

}