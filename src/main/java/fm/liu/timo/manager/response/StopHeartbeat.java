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
package fm.liu.timo.manager.response;

import java.util.Map;
import org.pmw.tinylog.Logger;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.parser.ManagerParseStop;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.mysql.OkPacket;
import fm.liu.timo.parser.util.Pair;
import fm.liu.timo.util.FormatUtil;
import fm.liu.timo.util.TimeUtil;

/**
 * 暂停数据节点心跳检测
 * 
 * @author xianmao.hexm
 */
public final class StopHeartbeat {

    public static void execute(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> keys = ManagerParseStop.getPair(stmt);
        if (keys.getKey() != null && keys.getValue() != null) {
            long time = keys.getValue().intValue() * 1000L;
            Map<Integer, Node> dns = TimoServer.getInstance().getConfig().getNodes();
            for (String key : keys.getKey()) {
                Node dn = dns.get(key);
                if (dn != null) {
                    dn.setHeartbeatRecoveryTime(TimeUtil.currentTimeMillis() + time);
                    ++count;
                    StringBuilder s = new StringBuilder();
                    s.append("datanode ").append(dn.getID()).append(" stop heartbeat '");
                    Logger.warn(s.append(FormatUtil.formatTime(time, 3)).append("' by manager.")
                            .toString());
                }
            }
        }
        OkPacket packet = new OkPacket();
        packet.packetId = 1;
        packet.affectedRows = count;
        packet.serverStatus = 2;
        packet.write(c);
    }

}
