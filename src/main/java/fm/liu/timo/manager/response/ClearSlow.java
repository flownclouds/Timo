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

import fm.liu.timo.TimoConfig;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.mysql.OkPacket;

/**
 * @author xianmao.hexm 2012-4-16
 */
public class ClearSlow {

    public static void dataNode(ManagerConnection c, String name) {
        Node dn = TimoServer.getInstance().getConfig().getNodes().get(name);
        if (dn != null && (dn.getSource() != null)) {
            // ds.getSqlRecorder().clear();
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid DataNode:" + name);
        }
    }

    public static void schema(ManagerConnection c, String name) {
        TimoConfig conf = TimoServer.getInstance().getConfig();
        Database schema = conf.getDatabases().get(name);
        if (schema != null) {
            Map<Integer, Node> dataNodes = conf.getNodes();
            for (Integer n : schema.getNodes()) {
                Node dn = dataNodes.get(n);
                if (dn != null && (dn.getSource() != null)) {
                    // ds.getSqlRecorder().clear();
                }
            }
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid Schema:" + name);
        }
    }

}
