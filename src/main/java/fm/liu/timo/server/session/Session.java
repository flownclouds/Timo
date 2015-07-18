/*
 * Copyright 2015 Liu Huanting.
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
package fm.liu.timo.server.session;

import java.util.Collection;

import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.route.Outlets;
import fm.liu.timo.server.ServerConnection;


/**
 * @author Liu Huanting
 *
 *         2015年7月18日
 */
public interface Session {

    /**
     * 
     * @return 前端连接
     */
    ServerConnection getFront();

    /**
     * 
     * @return 后端所有连接
     */
    Collection<BackendConnection> getConnections();

    /**
     * 
     * @param con 绑定后端连接到会话
     */
    void offer(BackendConnection con);

    /**
     * 执行SQL语句
     * 
     * @param outs 路由结果
     * @param type SQL类型
     */
    void execute(Outlets outs, int type);

}
