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
package re.ovo.timo.manager.handler;

import static re.ovo.timo.manager.parser.ManagerParseSwitch.DATASOURCE;
import re.ovo.timo.config.ErrorCode;
import re.ovo.timo.manager.ManagerConnection;
import re.ovo.timo.manager.parser.ManagerParseSwitch;
import re.ovo.timo.manager.response.SwitchDataSource;

/**
 * @author xianmao.hexm
 */
public final class SwitchHandler {

    public static void handler(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseSwitch.parse(stmt, offset)) {
            case DATASOURCE:
                SwitchDataSource.response(stmt, c);
                break;
            default:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
