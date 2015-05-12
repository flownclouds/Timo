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
package fm.liu.timo.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import fm.liu.timo.net.connection.AbstractConnection;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class NIOActor {
    private volatile SelectionKey key;
    private final AbstractConnection con;
    private final SocketChannel channel;
    private final AtomicBoolean writing = new AtomicBoolean(false);

    public NIOActor(AbstractConnection con) {
        this.con = con;
        this.channel = con.getChannel();
    }

    public void register(Selector selector) throws ClosedChannelException {
        key = channel.register(selector, SelectionKey.OP_READ, con);
    }

    public void read() throws IOException {
        ByteBuffer buffer = con.getReadBuffer();
        if (buffer == null) {
            buffer = con.getProcessor().getBufferPool().allocate();
            con.setReadBuffer(buffer);
        }
        int got = channel.read(buffer);
        if (got < 0) {
            con.close();
            return;
        } else if (got == 0) {
            if (!channel.isOpen()) {
                con.close();
            }
            return;
        }
        con.onRead(got);
    }

    public void check() {
        if (!writing.compareAndSet(false, true)) {
            return;
        }
        try {
            boolean finished = write();
            writing.set(false);
            if (finished && con.getWriteQueue().isEmpty()) {
                if ((key.isValid() && (key.interestOps() & SelectionKey.OP_WRITE) != 0)) {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
            } else {
                if ((key.isValid() && (key.interestOps() & SelectionKey.OP_WRITE) == 0)) {
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        } catch (IOException e) {
            con.close();
        }
    }

    private boolean write() throws IOException {
        int written = 0;
        ByteBuffer buffer = con.getWriteBuffer();
        if (buffer != null) {
            while (buffer.hasRemaining()) {
                written = channel.write(buffer);
                if (written <= 0) {
                    break;
                }
            }
            if (buffer.hasRemaining()) {
                return false;
            } else {
                con.setWriteBuffer(null);
                con.recycle(buffer);
            }
        }
        while ((buffer = con.getWriteQueue().poll()) != null) {
            if (buffer.limit() == 0) {
                con.recycle(buffer);
                con.close();
                return true;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                written = channel.write(buffer);
                if (written <= 0) {
                    break;
                }
            }
            if (buffer.hasRemaining()) {
                con.setWriteBuffer(buffer);
                return false;
            } else {
                con.recycle(buffer);
            }
        }
        return true;
    }
}
