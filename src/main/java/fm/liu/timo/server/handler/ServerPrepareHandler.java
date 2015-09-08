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

import java.io.UnsupportedEncodingException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.manager.response.ResponseUtil;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.mysql.ByteUtil;
import fm.liu.timo.mysql.PreparedStatement;
import fm.liu.timo.mysql.packet.ExecutePacket;
import fm.liu.timo.mysql.packet.OkPacket;
import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.UsrDefVarPrimary;
import fm.liu.timo.parser.ast.stmt.extension.DropPrepareStatement;
import fm.liu.timo.parser.ast.stmt.extension.ExecutePrepareStatement;
import fm.liu.timo.parser.ast.stmt.extension.PrepareStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;
import fm.liu.timo.server.ServerConnection;

/**
 * @author liuhuanting
 */
public class ServerPrepareHandler {
    private static final ArrayList<Head> fields = new ArrayList<Head>();

    static {
        fields.add(new Head("n"));
    }

    private ServerConnection source;

    public ServerPrepareHandler(ServerConnection source) {
        this.source = source;
    }

    public void prepare(String sql) {
        prepare(sql, source);
    }

    public void execute(byte[] data) {
        long pstmtId = ByteUtil.readUB4(data, 5);
        PreparedStatement pstmt = null;
        if ((pstmt = source.getPreparedStatement(pstmtId + "")) == null) {
            source.writeErrMessage(ErrorCode.ER_ERROR_WHEN_EXECUTING_COMMAND,
                    "Unknown pstmtId when executing.");
        } else {
            ExecutePacket packet = new ExecutePacket(pstmt);
            try {
                packet.read(data, source.getCharset());
            } catch (UnsupportedEncodingException e) {
                source.writeErrMessage(ErrorCode.ER_ERROR_WHEN_EXECUTING_COMMAND, e.getMessage());
                return;
            }
            execute(packet.getSQL(), source);
        }
    }

    public void close(byte[] data) {
        long pstmtId = ByteUtil.readUB4(data, 5);
        source.dropPreparedStatement(pstmtId + "");
        source.write(OkPacket.OK);
    }

    public static void prepare(String sql, ServerConnection c) {
        try {
            PrepareStatement stmt = (PrepareStatement) SQLParserDelegate.parse(sql);
            c.prepare(stmt);
            c.write(OkPacket.OK);
        } catch (SQLSyntaxErrorException e) {
            c.writeErrMessage(ErrorCode.ER_PARSE_ERROR, e.getMessage());
        }
    }

    public static void execute(String sql, ServerConnection c) {
        try {
            ExecutePrepareStatement stmt = (ExecutePrepareStatement) SQLParserDelegate.parse(sql);
            PreparedStatement prepare = c.getPreparedStatement(stmt.getName());
            if (prepare == null) {
                c.writeErrMessage(ErrorCode.ER_UNKNOWN_STMT_HANDLER,
                        "unknown prepared statement handler (" + stmt.getName()
                                + ") given to EXECUTE");
            } else {
                StringBuilder sb = new StringBuilder();
                String[] stmts = prepare.getStatements();
                int length = stmts.length;
                ArrayList<Object> values = new ArrayList<>();
                for (Expression var : stmt.getVars()) {
                    Expression exp =
                            c.getUserVariable(((UsrDefVarPrimary) var).getVarText().toLowerCase());
                    if (exp != null) {
                        values.add(exp.evaluation(Collections.emptyMap()));
                    } else {
                        ResponseUtil.write(c, fields, null);
                        return;
                    }
                }
                if (prepare.isEndsWithQuestionMark()) {
                    if (length != values.size()) {
                        c.writeErrMessage(ErrorCode.ER_WRONG_ARGUMENTS,
                                "Incorrect arguments to EXECUTE");
                        return;
                    } else {
                        for (int i = 0; i < length; i++) {
                            sb.append(stmts[i]).append(values.get(i).toString());
                        }
                    }
                } else {
                    if (length != values.size() + 1) {
                        c.writeErrMessage(ErrorCode.ER_WRONG_ARGUMENTS,
                                "Incorrect arguments to EXECUTE");
                        return;
                    } else {
                        for (int i = 0; i < length - 1; i++) {
                            sb.append(stmts[i]).append(values.get(i).toString());
                        }
                        sb.append(stmts[length - 1]);
                    }
                }
                c.query(sb.toString());
            }
        } catch (SQLSyntaxErrorException e) {
            c.writeErrMessage(ErrorCode.ER_PARSE_ERROR, e.getMessage());
        }
    }

    public static void close(String sql, ServerConnection c) {
        try {
            DropPrepareStatement stmt = (DropPrepareStatement) SQLParserDelegate.parse(sql);
            c.dropPreparedStatement(stmt.getName());
            c.write(OkPacket.OK);
        } catch (SQLSyntaxErrorException e) {
            c.writeErrMessage(ErrorCode.ER_PARSE_ERROR, e.getMessage());
        }
    }

}
