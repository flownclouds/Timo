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
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.parser.ManagerParseHandover;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.mysql.OkPacket;
import fm.liu.timo.parser.util.Pair;

/**
 * 切换数据节点的数据源
 * 
 * @author xianmao.hexm 2011-5-31 下午01:19:36
 */
public final class HandoverDatasource {

    public static void response(String stmt, ManagerConnection c) {
        Pair<String[], Integer> pair = ManagerParseHandover.getPair(stmt);
        Map<Integer, Node> nodes = TimoServer.getInstance().getConfig().getNodes();
        for (String key : pair.getKey()) {
            Node dn = nodes.get(Integer.parseInt(key));
            if (dn != null) {
                try {
                    if (dn.handover(dn.getSource().getConfig().getID())) {
                        OkPacket packet = new OkPacket();
                        packet.packetId = 1;
                        packet.affectedRows = 1;
                        packet.serverStatus = 2;
                        packet.write(c);
                        return;
                    } else {
                        ResponseUtil.error(c, "handover datasource failed");
                        return;
                    }
                } catch (Exception e) {
                    ResponseUtil.error(c, e.toString());
                    return;
                }
            }
        }

    }

}
