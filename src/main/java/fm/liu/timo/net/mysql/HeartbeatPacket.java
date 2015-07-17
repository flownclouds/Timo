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

/**
 * From client to server when the client do heartbeat between Timo cluster.
 * 
 * <pre>
 * Bytes Name ----- ---- 1 command n id
 * 
 */
public class HeartbeatPacket extends CommandPacket {

    public HeartbeatPacket() {
        super(CommandPacket.COM_HEARTBEAT);
    }

    public byte command;
    public long id;

    @Override
    protected void readBody(MySQLMessage mm) {
        command = mm.read();
        id = mm.readLength();
    }

    @Override
    public int calcPacketSize() {
        return 1 + BufferUtil.getLength(id);
    }

    @Override
    protected String getPacketInfo() {
        return "Timo Heartbeat Packet";
    }

    @Override
    protected void writeBody(ByteBuffer buffer) {
        buffer.put(command);
        BufferUtil.writeLength(buffer, id);
    }
}
