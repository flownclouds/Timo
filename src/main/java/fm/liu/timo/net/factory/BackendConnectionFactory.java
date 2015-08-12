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
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import fm.liu.timo.TimoServer;
import fm.liu.timo.config.model.Datasource;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.mysql.handler.MySQLAuthenticatorHandler;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.backend.Source;
import fm.liu.timo.net.connection.Variables;
import fm.liu.timo.net.handler.BackendConnectHandler;

/**
 * @author xianmao.hexm
 */
public abstract class BackendConnectionFactory {
    private final Variables variables;

    public BackendConnectionFactory(Variables variables) {
        this.variables = variables;
    }

    protected AsynchronousSocketChannel openSocketChannel(
            AsynchronousChannelGroup asynchronousChannelGroup) throws IOException {
        AsynchronousSocketChannel channel =
                AsynchronousSocketChannel.open(asynchronousChannelGroup);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, false);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        return channel;
    }

    public MySQLConnection newMySQLConnection(BackendConnectHandler handler, Datasource config,
            Source datasource) throws IOException {
        NIOProcessor processor = TimoServer.getInstance().nextProcessor();
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        MySQLConnection c = new MySQLConnection(channel, processor, datasource.getDatanodeID());
        c.setHost(config.getHost());
        c.setPort(config.getPort());
        c.setUsername(config.getUsername());
        c.setPassword(config.getPassword());
        c.setHandler(new MySQLAuthenticatorHandler(c, handler));
        c.setDatasource(datasource);
        c.getVariables().setCharsetIndex(variables.getCharsetIndex());
        c.getVariables().setIsolationLevel(variables.getIsolationLevel());
        TimoServer.getInstance().getConnector().postConnect(c);
        return c;
    }

}
