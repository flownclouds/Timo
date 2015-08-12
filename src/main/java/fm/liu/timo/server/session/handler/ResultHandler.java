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

import java.util.List;
import fm.liu.timo.net.connection.BackendConnection;

/**
 * @author Liu Huanting 2015年5月9日
 */
public interface ResultHandler {
    void ok(byte[] data, BackendConnection con);

    void error(byte[] data, BackendConnection con);

    void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con);

    void row(byte[] row, BackendConnection con);

    void eof(byte[] eof, BackendConnection con);

    void close(BackendConnection con, String reason);
}
