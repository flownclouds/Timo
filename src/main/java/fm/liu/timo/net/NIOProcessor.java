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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import fm.liu.timo.net.buffer.BufferPool;
import fm.liu.timo.net.connection.AbstractConnection;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.net.connection.NIOConnection;
import fm.liu.timo.statistic.CommandCount;
import fm.liu.timo.util.ExecutorUtil;
import fm.liu.timo.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public final class NIOProcessor {
    private static final int DEFAULT_BUFFER_SIZE       = 1024 * 1024 * 16;
    private static final int DEFAULT_BUFFER_CHUNK_SIZE = 4096;
    private static final int AVAILABLE_PROCESSORS      = Runtime.getRuntime().availableProcessors();

    private final String                                  name;
    private final NIOReactor                              reactor;
    private final BufferPool                              bufferPool;
    private final ExecutorService                         executor;
    private final ConcurrentMap<Long, FrontendConnection> frontends;
    private final ConcurrentMap<Long, BackendConnection>  backends;
    private final CommandCount                            commands;
    private long                                          netInBytes;
    private long                                          netOutBytes;
    private int                                           queryTimeout;

    public NIOProcessor(String name) throws IOException {
        this(name, DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_CHUNK_SIZE, AVAILABLE_PROCESSORS,
                AVAILABLE_PROCESSORS);
    }

    public NIOProcessor(String name, int handler, int executor, int queryTimeout)
            throws IOException {
        this(name, DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_CHUNK_SIZE, handler, executor);
        this.queryTimeout = queryTimeout;
    }

    public NIOProcessor(String name, int buffer, int chunk, int handler, int executor)
            throws IOException {
        this.name = name;
        this.reactor = new NIOReactor(name);
        this.bufferPool = new BufferPool(buffer, chunk);
        this.executor = (executor > 0) ? ExecutorUtil.create(name + "-E", executor) : null;
        this.frontends = new ConcurrentHashMap<Long, FrontendConnection>();
        this.backends = new ConcurrentHashMap<Long, BackendConnection>();
        this.commands = new CommandCount();
    }

    public String getName() {
        return name;
    }

    public BufferPool getBufferPool() {
        return bufferPool;
    }

    public int getRegisterQueueSize() {
        return reactor.getRegisterQueue().size();
    }

    public int getWriteQueueSize() {
        int total = 0;
        for (FrontendConnection fron : frontends.values()) {
            total += fron.getWriteQueue().size();
        }
        for (BackendConnection back : backends.values()) {
            total += back.getWriteQueue().size();
        }
        return total;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void startup() {
        reactor.startup();
    }

    public void postRegister(AbstractConnection c) {
        reactor.postRegister(c);
    }

    public CommandCount getCommands() {
        return commands;
    }

    public long getNetInBytes() {
        return netInBytes;
    }

    public void addNetInBytes(long bytes) {
        netInBytes += bytes;
    }

    public long getNetOutBytes() {
        return netOutBytes;
    }

    public void addNetOutBytes(long bytes) {
        netOutBytes += bytes;
    }

    public long getReactCount() {
        return reactor.getReactCount();
    }

    public void addFrontend(FrontendConnection c) {
        frontends.put(c.getId(), c);
    }

    public ConcurrentMap<Long, FrontendConnection> getFrontends() {
        return frontends;
    }

    public void addBackend(BackendConnection c) {
        backends.put(c.getID(), c);
    }

    public ConcurrentMap<Long, BackendConnection> getBackends() {
        return backends;
    }

    /**
     * 定时执行该方法，回收部分资源。
     */
    public void check() {
        frontendCheck();
        backendCheck();
    }

    // 前端连接检查
    private void frontendCheck() {
        Iterator<Entry<Long, FrontendConnection>> it = frontends.entrySet().iterator();
        while (it.hasNext()) {
            FrontendConnection c = it.next().getValue();
            // 删除空连接
            if (c == null) {
                it.remove();
                continue;
            }
            // 清理已关闭连接，否则空闲超时检查。
            if (c.isClosed()) {
                it.remove();
                c.cleanup();
            } else {
                // 空闲超时检查
                if (TimeUtil.currentTimeMillis() - c.getVariables().getLastActiveTime() > c
                        .getIdleTimeout()) {
                    c.close();
                }
            }
        }
    }

    // 后端连接检查
    private void backendCheck() {
        Iterator<Entry<Long, BackendConnection>> it = backends.entrySet().iterator();
        while (it.hasNext()) {
            BackendConnection c = it.next().getValue();
            // 删除空连接
            if (c == null) {
                it.remove();
                continue;
            }
            // 查询超时检查。
            if (c.isRunning() && TimeUtil.currentTimeMillis()
                    - c.getVariables().getLastActiveTime() > queryTimeout) {
                c.close();
            }
            // 清理已关闭连接
            if (c.isClosed()) {
                it.remove();
                c.cleanup();
            }
        }
    }

    public NIOReactor getReactor() {
        return reactor;
    }

    public void remove(NIOConnection con) {
        if (con instanceof FrontendConnection) {
            frontends.remove(con.getID());
        } else {
            backends.remove(con.getID());
        }
    }

}
