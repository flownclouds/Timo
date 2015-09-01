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
package fm.liu.timo.mysql.handler;

import fm.liu.timo.mysql.CharsetUtil;
import fm.liu.timo.mysql.SecurityUtil;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.mysql.packet.EOFPacket;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.mysql.packet.HandshakePacket;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.mysql.packet.Reply323Packet;
import fm.liu.timo.net.NIOHandler;
import fm.liu.timo.net.handler.BackendConnectHandler;

/**
 * MySQL连接认证
 * @author Liu Huanting 2015年5月9日
 */
public class AuthenticatorHandler implements NIOHandler {
    private final MySQLConnection       con;
    private final BackendConnectHandler handler;

    public AuthenticatorHandler(MySQLConnection con, BackendConnectHandler handler) {
        this.con = con;
        this.handler = handler;
    }

    @Override
    public void handle(byte[] data) {
        switch (data[4]) {
            case OkPacket.FIELD_COUNT:
                HandshakePacket handshake = con.getHandshake();
                if (handshake == null) {
                    prepare(data);
                    con.auth();
                    break;
                }
                con.setHandler(new ConnectorHandler(con));
                con.setAuthenticated(true);
                if (handler != null) {
                    handler.acquired(con);
                }
                break;
            case ErrorPacket.FIELD_COUNT:
                ErrorPacket err = new ErrorPacket();
                err.read(data);
                String msg = new String(err.message);
                con.close(msg);
                throw new RuntimeException(msg);
            case EOFPacket.FIELD_COUNT:
                auth323(data[3]);
                break;
            default:
                handshake = con.getHandshake();
                if (handshake == null) {
                    prepare(data);
                    // 发送认证数据包
                    con.auth();
                    break;
                } else {
                    throw new RuntimeException("unknown Packet!");
                }
        }
    }

    private void auth323(byte packetId) {
        // 发送323响应认证数据包
        Reply323Packet r323 = new Reply323Packet();
        r323.packetId = ++packetId;
        String pass = con.getPassword();
        if (pass != null && pass.length() > 0) {
            byte[] seed = con.getHandshake().seed;
            r323.seed = SecurityUtil.scramble323(pass, new String(seed)).getBytes();
        }
        r323.write(con);
    }

    private void prepare(byte[] data) {
        HandshakePacket packet = new HandshakePacket();
        packet.read(data);
        con.setHandshake(packet);
        con.setThreadID(packet.threadId);
        int charsetIndex = packet.serverCharsetIndex & 0xff;
        String charset = CharsetUtil.getCharset(charsetIndex);
        if (charset == null) {

        }
        if (charsetIndex != con.getVariables().getCharsetIndex()) {

        }
        boolean autocommit = false;
        if ((packet.serverStatus & 0x02) == 0x02) {
            autocommit = true;
        }
        con.getVariables().setAutocommit(autocommit);
    }

    public void error(Throwable t) {
        handler.error(t.getMessage(), con);
    }

}
