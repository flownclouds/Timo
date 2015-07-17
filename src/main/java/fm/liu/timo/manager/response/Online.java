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
/**
 * (created at 2011-11-22)
 */
package fm.liu.timo.manager.response;

import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.net.mysql.OkPacket;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class Online {

    private static final OkPacket ok = new OkPacket();

    static {
        ok.packetId = 1;
        ok.affectedRows = 1;
        ok.serverStatus = 2;
    }

    public static void execute(String stmt, ManagerConnection mc) {
        TimoServer.getInstance().online();
        ok.write(mc);
    }

}
