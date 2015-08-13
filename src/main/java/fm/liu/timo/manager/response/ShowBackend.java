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

import java.util.ArrayList;
import java.util.Collection;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.ManagerConnection;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.util.TimeUtil;

/**
 * 查询后端连接
 * 
 */
public class ShowBackend extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<Head>();

    static {
        heads.add(new Head("processor", "所属的Processor"));
        heads.add(new Head("id", "编号"));
        heads.add(new Head("connect_id", "连接号"));
        heads.add(new Head("dnid", "DNID"));
        heads.add(new Head("host", "主机信息"));
        heads.add(new Head("db", "物理数据库"));
        heads.add(new Head("port", "本地端口号"));
        heads.add(new Head("up_time", "启动时长（秒）"));
        //        heads.add(new Head("state", "连接状态"));
        heads.add(new Head("send_queue", "发送队列大小"));
    }

    @Override
    public String getInfo() {
        return "显示后端连接情况";
    }

    @Override
    public void execute(ManagerConnection c) {
        ResponseUtil.write(c, heads, getRows());
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        NIOProcessor[] processors = TimoServer.getInstance().getProcessors();
        for (NIOProcessor processor : processors) {
            Collection<BackendConnection> backends = processor.getBackends().values();
            for (BackendConnection backend : backends) {
                if (backend != null) {
                    Object[] row = new Object[heads.size()];
                    int i = 0;
                    row[i++] = processor.getName();
                    row[i++] = backend.getID();
                    row[i++] = ((MySQLConnection) backend).getThreadID();
                    row[i++] = backend.getDatanodeID();
                    row[i++] = backend.getHost() + ":" + backend.getPort();
                    row[i++] = ((MySQLConnection) backend).getDatasource().getConfig().getDB();
                    row[i++] = backend.getLocalPort();
                    row[i++] = (TimeUtil.currentTimeMillis() - backend.getVariables().getUpTime())
                            / 1000;
                    row[i++] = ((MySQLConnection) backend).getWriteQueue().size();
                    rows.add(row);
                }
            }
        }
        return rows;
    }

}
