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
package re.ovo.timo.mysql.handler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import re.ovo.timo.config.ErrorCode;
import re.ovo.timo.net.connection.BackendConnection;
import re.ovo.timo.net.mysql.ErrorPacket;
import re.ovo.timo.net.mysql.OkPacket;
import re.ovo.timo.server.ServerConnection;
import re.ovo.timo.server.session.AbstractSession;
import re.ovo.timo.server.session.handler.SessionResultHandler;
import re.ovo.timo.util.StringUtil;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class MySQLMultiNodeHandler extends SessionResultHandler {
    protected long affectedRows = 0;
    protected long insertId = 0;
    protected boolean returned = false;

    public MySQLMultiNodeHandler(AbstractSession session, int size) {
        super.session = session;
        super.count = new AtomicInteger(size);
    }

    @Override
    public void ok(byte[] data, BackendConnection con) {
        con.release();
        if (failed()) {
            if (decrement()) {
                onError();
            }
            return;
        }
        OkPacket ok = new OkPacket();
        ok.read(data);
        lock.lock();
        try {
            affectedRows += ok.affectedRows;
            if (ok.insertId > 0) {
                insertId = (insertId == 0) ? ok.insertId : Math.min(insertId, ok.insertId);
            }
        } finally {
            lock.unlock();
        }
        if (decrement()) {
            ok.packetId = ++packetId;
            ok.affectedRows = affectedRows;
            if (insertId > 0) {
                ok.insertId = insertId;
            }
            ok.write(session.getFront());
        }
    }

    @Override
    public void error(byte[] data, BackendConnection con) {
        con.release();
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        String errmsg = new String(err.message);
        setFail(err.errno, errmsg);
        if (decrement()) {
            onError();
        }
    }

    private void onError() {
        ErrorPacket err = new ErrorPacket();
        err.packetId = 1;
        err.errno = errno;
        err.message = StringUtil.encode(errMsg, session.getFront().getCharset());
        err.write(session.getFront());
    }

    @Override
    public void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con) {
        if (failed()) {
            return;
        }
        lock.lock();
        allocBuffer();
        try {
            if (returned) {
                return;
            }
            returned = true;
            header[3] = ++packetId;
            ServerConnection source = session.getFront();
            buffer = source.writeToBuffer(header, allocBuffer());
            for (int i = 0, len = fields.size(); i < len; ++i) {
                byte[] field = fields.get(i);
                field[3] = ++packetId;
                buffer = source.writeToBuffer(field, buffer);
            }
            eof[3] = ++packetId;
            buffer = source.writeToBuffer(eof, buffer);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void row(byte[] row, BackendConnection con) {
        if (failed()) {
            return;
        }
        lock.lock();
        try {
            row[3] = ++packetId;
            buffer = session.getFront().writeToBuffer(row, allocBuffer());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void eof(byte[] eof, BackendConnection con) {
        con.release();
        if (decrement()) {
            if (failed()) {
                onError();
                return;
            }
            ServerConnection front = session.getFront();
            eof[3] = ++packetId;
            buffer = front.writeToBuffer(eof, allocBuffer());
            front.write(buffer);
        }
    }

    @Override
    public void close(BackendConnection con, String reason) {
        if(decrement()){
            ErrorPacket err = new ErrorPacket();
            err.packetId = ++packetId;
            err.errno = ErrorCode.ER_YES;
            err.message = StringUtil.encode(reason, session.getFront()
                    .getCharset());
            err.write(session.getFront());
        }
    }

}
