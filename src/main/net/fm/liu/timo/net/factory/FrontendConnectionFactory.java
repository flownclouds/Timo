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
package fm.liu.timo.net.factory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.net.connection.Variables;

/**
 * @author xianmao.hexm
 */
public abstract class FrontendConnectionFactory {

    protected int socketRecvBuffer = 8 * 1024;
    protected int socketSendBuffer = 16 * 1024;
    protected int writeQueueCapcity = 16;
    protected long idleTimeout = 8 * 3600 * 1000L;
    protected final Variables variables;

    public FrontendConnectionFactory(Variables variables) {
        this.variables = variables;
    }

    protected abstract FrontendConnection getConnection(SocketChannel channel,
            NIOProcessor processor);

    public FrontendConnection make(SocketChannel channel, NIOProcessor processor)
            throws IOException {
        channel.setOption(StandardSocketOptions.SO_RCVBUF, socketRecvBuffer);
        channel.setOption(StandardSocketOptions.SO_SNDBUF, socketSendBuffer);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

        FrontendConnection c = getConnection(channel, processor);
        c.setIdleTimeout(idleTimeout);
        return c;
    }

    public int getSocketRecvBuffer() {
        return socketRecvBuffer;
    }

    public void setSocketRecvBuffer(int socketRecvBuffer) {
        this.socketRecvBuffer = socketRecvBuffer;
    }

    public int getSocketSendBuffer() {
        return socketSendBuffer;
    }

    public void setSocketSendBuffer(int socketSendBuffer) {
        this.socketSendBuffer = socketSendBuffer;
    }

    public int getWriteQueueCapcity() {
        return writeQueueCapcity;
    }

    public void setWriteQueueCapcity(int writeQueueCapcity) {
        this.writeQueueCapcity = writeQueueCapcity;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

}
