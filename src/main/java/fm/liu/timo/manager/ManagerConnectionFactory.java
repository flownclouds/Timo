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
package fm.liu.timo.manager;

import java.nio.channels.SocketChannel;

import fm.liu.timo.TimoPrivileges;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.net.connection.Variables;
import fm.liu.timo.net.factory.FrontendConnectionFactory;

/**
 * @author xianmao.hexm
 */
public class ManagerConnectionFactory extends FrontendConnectionFactory {

    public ManagerConnectionFactory(Variables variables) {
        super(variables);
    }

    @Override
    protected FrontendConnection getConnection(SocketChannel channel, NIOProcessor processor) {
        ManagerConnection c = new ManagerConnection(channel, processor);
        c.setPrivileges(new TimoPrivileges());
        c.setQueryHandler(new ManagerQueryHandler(c));
        return c;
    }

}
