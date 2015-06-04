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

public class Reply323Packet extends MySQLClientPacket {

	public byte[] seed;

	@Override
	public int calcPacketSize() {
		return seed == null ? 1 : seed.length + 1;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Auth323 Packet";
	}

	@Override
	protected void writeBody(ByteBuffer buffer) {
		if (seed == null) {
			buffer.put((byte) 0);
		} else {
			BufferUtil.writeWithNull(buffer, seed);
		}		
	}

	@Override
	protected void readBody(MySQLMessage mm) {
		throw new RuntimeException("readBody for Reply323Packet not implement!");	
	}

}