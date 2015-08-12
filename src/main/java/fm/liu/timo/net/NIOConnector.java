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
package fm.liu.timo.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.pmw.tinylog.Logger;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.net.connection.AbstractConnection;
import fm.liu.timo.net.connection.BackendConnection;

/**
 * @author xianmao.hexm
 */
public final class NIOConnector extends Thread {
    private static final ConnectIdGenerator ID_GENERATOR = new ConnectIdGenerator();

    private final String                           name;
    private final Selector                         selector;
    private final BlockingQueue<BackendConnection> connectQueue;
    private long                                   connectCount;

    public NIOConnector(String name) throws IOException {
        super.setName(name);
        this.name = name;
        this.selector = Selector.open();
        this.connectQueue = new LinkedBlockingQueue<BackendConnection>();
    }

    public long getConnectCount() {
        return connectCount;
    }

    public void postConnect(BackendConnection c) {
        connectQueue.offer(c);
        selector.wakeup();
    }

    @Override
    public void run() {
        final Selector selector = this.selector;
        for (;;) {
            ++connectCount;
            try {
                selector.select(1000L);
                connect(selector);
                Set<SelectionKey> keys = selector.selectedKeys();
                try {
                    for (SelectionKey key : keys) {
                        Object att = key.attachment();
                        if (att != null && key.isValid() && key.isConnectable()) {
                            finishConnect(key, att);
                        } else {
                            key.cancel();
                        }
                    }
                } finally {
                    keys.clear();
                }
            } catch (Throwable e) {
                Logger.warn("Thread {} exception:{}", name, e);
            }
        }
    }

    private void connect(Selector selector) {
        AbstractConnection c = null;
        while ((c = connectQueue.poll()) != null) {
            try {
                SocketChannel channel = (SocketChannel) c.getChannel();
                channel.register(selector, SelectionKey.OP_CONNECT, c);
                channel.connect(new InetSocketAddress(c.getHost(), c.getPort()));
            } catch (Throwable e) {
                e.printStackTrace();
                c.close();
            }
        }
    }

    private void finishConnect(SelectionKey key, Object att) {
        BackendConnection c = (BackendConnection) att;
        try {
            if (finishConnect(c, (SocketChannel) c.getChannel())) {
                clearSelectionKey(key);
                c.setID(ID_GENERATOR.getId());
                c.getProcessor().addBackend(c);
                MySQLConnection msqlCon = (MySQLConnection) c;
                msqlCon.getDatasource().add(c);
                NIOProcessor processor = (NIOProcessor) c.getProcessor();
                NIOReactor reactor = processor.getReactor();
                reactor.postRegister(c);
            }
        } catch (Throwable e) {
            clearSelectionKey(key);
            c.onConnectFailed(e);
            c.close();
        }
    }

    private boolean finishConnect(BackendConnection c, SocketChannel channel) throws IOException {
        if (channel.isConnectionPending()) {
            channel.finishConnect();
            c.finishConnect();
            return true;
        } else {
            return false;
        }
    }

    private void clearSelectionKey(SelectionKey key) {
        if (key.isValid()) {
            key.attach(null);
            key.cancel();
        }
    }

    /**
     * 后端连接ID生成器
     * 
     * @author xianmao.hexm
     */
    private static class ConnectIdGenerator {

        private static final long MAX_VALUE = Long.MAX_VALUE;

        private long         connectId = 0L;
        private final Object lock      = new Object();

        private long getId() {
            synchronized (lock) {
                if (connectId >= MAX_VALUE) {
                    connectId = 0L;
                }
                return ++connectId;
            }
        }
    }

}
