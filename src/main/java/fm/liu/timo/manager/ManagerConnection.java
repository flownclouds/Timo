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
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-22 下午02:23:55
 */
public class ManagerConnection extends FrontendConnection {
    private static final long AUTH_TIMEOUT = 15 * 1000L;

    public ManagerConnection(SocketChannel channel, NIOProcessor processor) {
        super(channel, processor);
    }

    @Override
    public boolean isIdleTimeout() {
        if (isAuthenticated) {
            return super.isIdleTimeout();
        } else {
            return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime)
                    + AUTH_TIMEOUT;
        }
    }

    @Override
    public void close(String reason) {
        if (super.closed.compareAndSet(false, true)) {
            processor.remove(this);
            super.cleanup();
        }
    }

}
