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
package fm.liu.timo.net.connection;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author xianmao.hexm
 */
public interface NIOConnection {

    /**
     * 注册网络事件
     */
    void register() throws IOException;

    /**
     * 处理数据
     */
    void handle(byte[] data);

    /**
     * 写出一块缓存数据
     */
    void write(ByteBuffer buffer);

    /**
     * 发生错误
     */
    void error(int errCode, Throwable t);

    /**
     * 关闭连接
     */
    void close(String reason);

    long getID();

    public void setState(int state);

}
