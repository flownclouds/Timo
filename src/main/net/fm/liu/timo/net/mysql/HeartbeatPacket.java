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
 * From client to server when the client do heartbeat between hotdb cluster.
 * 
 * <pre>
 * Bytes         Name
 * -----         ----
 * 1             command
 * n             id
 * 
 * @author hotdb
 */
public class HeartbeatPacket extends CommandPacket {

	public HeartbeatPacket() {
		super(CommandPacket.COM_HEARTBEAT);
	}

	public byte command;
	public long id;

	@Override
	protected void readBody(MySQLMessage mm){
		command = mm.read();
		id = mm.readLength();
	}

	@Override
	public int calcPacketSize() {
		return 1 + BufferUtil.getLength(id);
	}

	@Override
	protected String getPacketInfo() {
		return "Mycat Heartbeat Packet";
	}

	@Override
	protected void writeBody(ByteBuffer buffer) {
		buffer.put(command);
		BufferUtil.writeLength(buffer, id);
	}
}