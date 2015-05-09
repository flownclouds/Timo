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
package re.ovo.timo.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import re.ovo.timo.config.ErrorCode;
import re.ovo.timo.net.connection.AbstractConnection;

/**
 * 网络事件反应器
 * 
 * @author xianmao.hexm
 */
public final class NIOReactor {
    private static final Logger LOGGER = Logger.getLogger(NIOReactor.class);

    private final String name;
    private final RW reactor;

    public NIOReactor(String name) throws IOException {
        this.name = name;
        this.reactor = new RW();
    }

    final void startup() {
        new Thread(reactor, name + "-RW").start();
    }

    final void postRegister(AbstractConnection c) {
        reactor.registerQueue.offer(c);
        reactor.selector.wakeup();
    }

    final Queue<AbstractConnection> getRegisterQueue() {
        return reactor.registerQueue;
    }

    final long getReactCount() {
        return reactor.reactCount;
    }

    private final class RW implements Runnable {
        private final Selector selector;
        private final ConcurrentLinkedQueue<AbstractConnection> registerQueue;
        private long reactCount;

        private RW() throws IOException {
            this.selector = Selector.open();
            this.registerQueue = new ConcurrentLinkedQueue<AbstractConnection>();
        }

        @Override
        public void run() {
            final Selector selector = this.selector;
            for (;;) {
                ++reactCount;
                try {
                    selector.select(500L);
                    register(selector);
                    Set<SelectionKey> keys = selector.selectedKeys();
                    try {
                        for (SelectionKey key : keys) {
                            Object att = key.attachment();
                            if (att != null && key.isValid()) {
                                AbstractConnection con = (AbstractConnection) att;
                                if (key.isReadable()) {
                                    read(con);
                                }
                                if (key.isWritable()) {
                                    con.check();
                                }
                            } else {
                                key.cancel();
                            }
                        }
                    } finally {
                        keys.clear();
                    }
                } catch (Throwable e) {
                    LOGGER.warn(name, e);
                }
            }
        }

        private void register(Selector selector) {
            AbstractConnection c = null;
            while ((c = registerQueue.poll()) != null) {
                try {
                    c.getActor().register(selector);
                    c.register();
                } catch (Throwable e) {
                    c.error(ErrorCode.ERR_REGISTER, e);
                }
            }
        }

        private void read(AbstractConnection c) {
            try {
                c.read();
            } catch (Throwable e) {
                c.error(ErrorCode.ERR_READ, e);
            }
        }
    }

}
