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
package fm.liu.timo.server.session.handler;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import fm.liu.timo.mysql.packet.ErrorPacket;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.server.session.Session;
import fm.liu.timo.server.session.TransactionSession;
import fm.liu.timo.util.StringUtil;

/**
 * @author Liu Huanting 2015年5月9日
 */
public abstract class SessionResultHandler implements ResultHandler {
    public Session                   session;
    protected volatile byte          packetId = 0;
    protected volatile ByteBuffer    buffer   = null;
    protected volatile AtomicInteger count;
    protected volatile AtomicBoolean failed   = new AtomicBoolean(false);
    protected final ReentrantLock    lock     = new ReentrantLock();
    protected volatile int           errno;
    protected volatile String        errMsg;
    protected Set<BackendConnection> savedConnections;

    protected void recycleResources() {
        if (buffer != null) {
            ByteBuffer temp = buffer;
            buffer = null;
            session.getFront().recycle(temp);
        }
    }

    /**
     * lazy create ByteBuffer only when needed
     * 
     * @return
     */
    protected ByteBuffer allocBuffer() {
        if (buffer == null) {
            buffer = session.getFront().allocate();
        }
        return buffer;
    }

    protected boolean decrement() {
        return count.decrementAndGet() == 0;
    }

    protected void onError() {
        ErrorPacket err = new ErrorPacket();
        err.packetId = 1;
        err.errno = errno;
        err.message = StringUtil.encode(errMsg, session.getFront().getCharset());
        if (session instanceof TransactionSession) {
            ((TransactionSession) session).rollbackToSavepoint(err);
        } else {
            err.write(session.getFront());
        }
        recycleResources();
    }

    protected void setFail(int errno, String errMsg) {
        failed.set(true);
        this.errno = errno;
        this.errMsg = errMsg;
    }

    protected boolean failed() {
        return failed.get();
    }

    @Override
    public void close(String reason) {
        this.errMsg = reason;
        onError();
    }

    public void setSQL(String sql) {};
}
