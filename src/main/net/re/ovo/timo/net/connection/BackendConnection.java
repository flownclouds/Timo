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
package re.ovo.timo.net.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import re.ovo.timo.net.NIOProcessor;
import re.ovo.timo.route.RouteResultsetNode;
import re.ovo.timo.server.session.handler.ResultHandler;

/**
 * @author xianmao.hexm
 */
public abstract class BackendConnection extends AbstractConnection {

    public BackendConnection(SocketChannel channel, NIOProcessor processor) {
        super(channel, processor);
    }

    public void register() throws IOException {
        this.read();
    }

    public abstract int getDatanodeID();
    
    public abstract void onConnectFailed(Throwable e);

    public abstract void query(RouteResultsetNode rrn, ResultHandler handler);

    public abstract void release();

    public boolean finishConnect() throws IOException {
        // 后端属于主动链接，当connectiong对象构造时，链路还没有建立成功
        localPort = ((InetSocketAddress) channel.getLocalAddress()).getPort();
        return true;
    }

}
