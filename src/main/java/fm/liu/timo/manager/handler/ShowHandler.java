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

import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.parser.ManagerParseShow;
import fm.liu.timo.manager.response.ShowBackend;
import fm.liu.timo.manager.response.ShowCollation;
import fm.liu.timo.manager.response.ShowCommand;
import fm.liu.timo.manager.response.ShowConnection;
import fm.liu.timo.manager.response.ShowConnectionSQL;
import fm.liu.timo.manager.response.ShowDataNode;
import fm.liu.timo.manager.response.ShowDataSource;
import fm.liu.timo.manager.response.ShowDatabase;
import fm.liu.timo.manager.response.ShowHeartbeat;
import fm.liu.timo.manager.response.ShowHelp;
import fm.liu.timo.manager.response.ShowParser;
import fm.liu.timo.manager.response.ShowProcessor;
import fm.liu.timo.manager.response.ShowRouter;
import fm.liu.timo.manager.response.ShowSQL;
import fm.liu.timo.manager.response.ShowSQLDetail;
import fm.liu.timo.manager.response.ShowSQLExecute;
import fm.liu.timo.manager.response.ShowServer;
import fm.liu.timo.manager.response.ShowThreadPool;
import fm.liu.timo.manager.response.ShowTime;
import fm.liu.timo.manager.response.ShowVariables;
import fm.liu.timo.manager.response.ShowVersion;
import fm.liu.timo.parser.util.ParseUtil;
import fm.liu.timo.util.StringUtil;

/**
 * @author xianmao.hexm
 */
public final class ShowHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        int rs = ManagerParseShow.parse(stmt, offset);
        switch (rs & 0xff) {
            case ManagerParseShow.COMMAND:
                ShowCommand.execute(c);
                break;
            case ManagerParseShow.COLLATION:
                ShowCollation.execute(c);
                break;
            case ManagerParseShow.CONNECTION:
                ShowConnection.execute(c);
                break;
            case ManagerParseShow.BACKEND:
                ShowBackend.execute(c);
                break;
            case ManagerParseShow.CONNECTION_SQL:
                ShowConnectionSQL.execute(c);
                break;
            case ManagerParseShow.DATABASE:
                ShowDatabase.execute(c);
                break;
            case ManagerParseShow.DATANODE:
                ShowDataNode.execute(c, null);
                break;
            case ManagerParseShow.DATANODE_WHERE: {
                String name = stmt.substring(rs >>> 8).trim();
                if (StringUtil.isEmpty(name)) {
                    c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
                } else {
                    ShowDataNode.execute(c, name);
                }
                break;
            }
            case ManagerParseShow.DATASOURCE:
                ShowDataSource.execute(c, 0);
                break;
            case ManagerParseShow.DATASOURCE_WHERE: {
                String name = stmt.substring(rs >>> 8).trim();
                if (StringUtil.isEmpty(name)) {
                    c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
                } else {
                    ShowDataSource.execute(c, Integer.valueOf(name));
                }
                break;
            }
            case ManagerParseShow.HELP:
                ShowHelp.execute(c);
                break;
            case ManagerParseShow.HEARTBEAT:
                ShowHeartbeat.response(c);
                break;
            case ManagerParseShow.PARSER:
                ShowParser.execute(c);
                break;
            case ManagerParseShow.PROCESSOR:
                ShowProcessor.execute(c);
                break;
            case ManagerParseShow.ROUTER:
                ShowRouter.execute(c);
                break;
            case ManagerParseShow.SERVER:
                ShowServer.execute(c);
                break;
            case ManagerParseShow.SQL:
                ShowSQL.execute(c, ParseUtil.getSQLId(stmt));
                break;
            case ManagerParseShow.SQL_DETAIL:
                ShowSQLDetail.execute(c, ParseUtil.getSQLId(stmt));
                break;
            case ManagerParseShow.SQL_EXECUTE:
                ShowSQLExecute.execute(c);
                break;
            case ManagerParseShow.THREADPOOL:
                ShowThreadPool.execute(c);
                break;
            case ManagerParseShow.TIME_CURRENT:
                ShowTime.execute(c, ManagerParseShow.TIME_CURRENT);
                break;
            case ManagerParseShow.TIME_STARTUP:
                ShowTime.execute(c, ManagerParseShow.TIME_STARTUP);
                break;
            case ManagerParseShow.VARIABLES:
                ShowVariables.execute(c);
                break;
            case ManagerParseShow.VERSION:
                ShowVersion.execute(c);
                break;
            default:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }
}
