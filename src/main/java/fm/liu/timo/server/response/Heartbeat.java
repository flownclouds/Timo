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
package fm.liu.timo.server.response;

import org.pmw.tinylog.Logger;

import fm.liu.timo.TimoServer;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.net.mysql.ErrorPacket;
import fm.liu.timo.net.mysql.HeartbeatPacket;
import fm.liu.timo.net.mysql.OkPacket;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.util.TimeUtil;

public class Heartbeat {


    public static void response(ServerConnection c, byte[] data) {
        HeartbeatPacket hp = new HeartbeatPacket();
        hp.read(data);
        if (TimoServer.getInstance().isOnline()) {
            OkPacket ok = new OkPacket();
            ok.packetId = 1;
            ok.affectedRows = hp.id;
            ok.serverStatus = 2;
            ok.write(c);
            if (Logger.isInfoEnabled()) {
                Logger.info(responseMessage("OK", c, hp.id));
            }
        } else {
            ErrorPacket error = new ErrorPacket();
            error.packetId = 1;
            error.errno = ErrorCode.ER_SERVER_SHUTDOWN;
            error.message = String.valueOf(hp.id).getBytes();
            error.write(c);
            if (Logger.isInfoEnabled()) {
                Logger.info(responseMessage("ERROR", c, hp.id));
            }
        }
    }

    private static String responseMessage(String action, ServerConnection c, long id) {
        return new StringBuilder("RESPONSE:").append(action).append(", id=").append(id)
                .append(", host=").append(c.getHost()).append(", port=").append(c.getPort())
                .append(", time=").append(TimeUtil.currentTimeMillis()).toString();
    }

}
