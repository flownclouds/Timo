/*
 * Copyright 1999-2012 Alibaba Group.
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
package fm.liu.timo.server.handler;

import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.config.Isolations;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.server.parser.ServerParseSet;
import fm.liu.timo.server.response.CharacterSet;

/**
 * SET 语句处理
 * 
 * @author xianmao.hexm
 */
public final class SetHandler {

    public static void handle(String stmt, ServerConnection c, int offset) {
        int rs = ServerParseSet.parse(stmt, offset);
        switch (rs & 0xff) {
            case ServerParseSet.AUTOCOMMIT_ON:
                c.setAutocommit(true);
                break;
            case ServerParseSet.AUTOCOMMIT_OFF:
                c.setAutocommit(false);
                break;
            case ServerParseSet.TX_READ_UNCOMMITTED:
                c.setIsolationLevel(Isolations.READ_UNCOMMITTED);
                break;
            case ServerParseSet.TX_READ_COMMITTED:
                c.setIsolationLevel(Isolations.READ_COMMITTED);
                break;
            case ServerParseSet.TX_REPEATED_READ:
                c.setIsolationLevel(Isolations.REPEATED_READ);
                break;
            case ServerParseSet.TX_SERIALIZABLE:
                c.setIsolationLevel(Isolations.SERIALIZABLE);
                break;
            case ServerParseSet.NAMES:
                String charset = stmt.substring(rs >>> 8).trim();
                if (c.setCharset(charset)) {
                    c.write(OkPacket.OK);
                } else {
                    c.writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET,
                            "Unknown charset '" + charset + "'");
                }
                break;
            case ServerParseSet.CHARACTER_SET_CLIENT:
            case ServerParseSet.CHARACTER_SET_CONNECTION:
            case ServerParseSet.CHARACTER_SET_RESULTS:
                CharacterSet.response(stmt, c, rs);
                break;
            default:
                c.write(OkPacket.OK);
        }
    }

}
