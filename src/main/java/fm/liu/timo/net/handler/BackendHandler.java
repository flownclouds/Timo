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
package fm.liu.timo.net.handler;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import fm.liu.timo.net.NIOHandler;

/**
 * @author Liu Huanting 2015年5月9日
 */
public abstract class BackendHandler implements NIOHandler {
    protected final LinkedBlockingQueue<byte[]> dataQueue  = new LinkedBlockingQueue<byte[]>();
    protected final AtomicBoolean               isHandling = new AtomicBoolean(false);

    protected void offerData(byte[] data, Executor executor) {
        if (dataQueue.offer(data)) {
            handleQueue();
        } else {
            offerDataError();
        }
    }

    protected abstract void offerDataError();

    protected abstract void handleData(byte[] data);

    protected abstract void handleDataError(Throwable t);

    protected void handleQueue() {
        if (isHandling.compareAndSet(false, true)) {
            try {
                byte[] data = null;
                while ((data = dataQueue.poll()) != null) {
                    handleData(data);
                }
            } catch (Throwable t) {
                handleDataError(t);
            } finally {
                isHandling.set(false);
                if (!dataQueue.isEmpty()) {
                    handleQueue();
                }
            }
        }

    }

}
