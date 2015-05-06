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
package re.ovo.timo.server.response;

import re.ovo.timo.TimoServer;
import re.ovo.timo.mysql.PacketUtil;
import re.ovo.timo.net.FrontendConnection;
import re.ovo.timo.net.mysql.ErrorPacket;
import re.ovo.timo.net.mysql.OkPacket;

/**
 * 加入了offline状态推送，用于心跳语句。
 * 
 * @author xianmao.hexm 2012-4-28
 */
public class Ping {

    private static final ErrorPacket error = PacketUtil.getShutdown();

    public static void response(FrontendConnection c) {
        if (TimoServer.getInstance().isOnline()) {
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            error.write(c);
        }
    }

}
