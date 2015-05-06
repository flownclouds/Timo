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

import re.ovo.timo.config.ErrorCode;
import re.ovo.timo.manager.ManagerConnection;
import re.ovo.timo.manager.parser.ManagerParseRollback;
import re.ovo.timo.manager.response.RollbackConfig;
import re.ovo.timo.manager.response.RollbackUser;

/**
 * @author xianmao.hexm
 */
public final class RollbackHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseRollback.parse(stmt, offset)) {
            case ManagerParseRollback.CONFIG:
                RollbackConfig.execute(c);
                break;
            case ManagerParseRollback.ROUTE:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
                break;
            case ManagerParseRollback.USER:
                RollbackUser.execute(c);
                break;
            default:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
