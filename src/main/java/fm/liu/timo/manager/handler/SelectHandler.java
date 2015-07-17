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
package fm.liu.timo.manager.handler;

import static fm.liu.timo.manager.parser.ManagerParseSelect.SESSION_AUTO_INCREMENT;
import static fm.liu.timo.manager.parser.ManagerParseSelect.VERSION_COMMENT;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.parser.ManagerParseSelect;
import fm.liu.timo.manager.response.SelectSessionAutoIncrement;
import fm.liu.timo.manager.response.SelectVersionComment;

/**
 * @author xianmao.hexm
 */
public final class SelectHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseSelect.parse(stmt, offset)) {
            case VERSION_COMMENT:
                SelectVersionComment.execute(c);
                break;
            case SESSION_AUTO_INCREMENT:
                SelectSessionAutoIncrement.execute(c);
                break;
            default:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
