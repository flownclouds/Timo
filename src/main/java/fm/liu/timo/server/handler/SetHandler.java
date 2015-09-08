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

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.config.Isolations;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.UsrDefVarPrimary;
import fm.liu.timo.parser.ast.expression.primary.VariableExpression;
import fm.liu.timo.parser.ast.expression.primary.literal.Literal;
import fm.liu.timo.parser.ast.stmt.dal.DALSetStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;
import fm.liu.timo.parser.util.Pair;
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
                c.write(OkPacket.OK);
                break;
            case ServerParseSet.TX_READ_COMMITTED:
                c.setIsolationLevel(Isolations.READ_COMMITTED);
                c.write(OkPacket.OK);
                break;
            case ServerParseSet.TX_REPEATED_READ:
                c.setIsolationLevel(Isolations.REPEATED_READ);
                c.write(OkPacket.OK);
                break;
            case ServerParseSet.TX_SERIALIZABLE:
                c.setIsolationLevel(Isolations.SERIALIZABLE);
                c.write(OkPacket.OK);
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
            case ServerParseSet.USER_VAR:
                try {
                    DALSetStatement set = (DALSetStatement) SQLParserDelegate.parse(stmt);
                    ;
                    List<Pair<VariableExpression, Expression>> list = set.getAssignmentList();
                    for (Pair<VariableExpression, Expression> pair : list) {
                        VariableExpression key = pair.getKey();
                        Expression value = pair.getValue();
                        if (!(value instanceof Literal)) {
                            throw new SQLSyntaxErrorException(
                                    "unsupported user variables for : " + stmt);
                        }
                        c.setUserVariable(((UsrDefVarPrimary) key).getVarText().toLowerCase(),
                                value);
                    }
                    c.write(OkPacket.OK);
                } catch (SQLSyntaxErrorException e) {
                    // 注意！默认返回OK！不代表用户变量设置成功了！
                    c.write(OkPacket.OK);
                    Logger.warn(e.getMessage());
                    return;
                }
                break;
            default:
                c.write(OkPacket.OK);
        }
    }

}
