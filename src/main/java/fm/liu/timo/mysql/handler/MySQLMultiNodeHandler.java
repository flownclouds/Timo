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
package fm.liu.timo.mysql.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.merger.ColumnInfo;
import fm.liu.timo.merger.Merger;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.net.mysql.ErrorPacket;
import fm.liu.timo.net.mysql.FieldPacket;
import fm.liu.timo.net.mysql.OkPacket;
import fm.liu.timo.net.mysql.RowDataPacket;
import fm.liu.timo.server.ServerConnection;
import fm.liu.timo.server.session.AbstractSession;
import fm.liu.timo.server.session.handler.SessionResultHandler;
import fm.liu.timo.util.StringUtil;

/**
 * @author Liu Huanting 2015年5月9日
 */
public class MySQLMultiNodeHandler extends SessionResultHandler {
    protected long affectedRows = 0;
    protected long insertId = 0;
    protected boolean returned = false;
    protected Merger merger;

    public MySQLMultiNodeHandler(AbstractSession session, Merger merger, int size) {
        super.session = session;
        super.count = new AtomicInteger(size);
        this.merger = merger;
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
            Map<String, ColumnInfo> columnInfos = new HashMap<String, ColumnInfo>();
            header[3] = ++packetId;
            ServerConnection front = session.getFront();
            buffer = front.writeToBuffer(header, allocBuffer());
            int fieldCount = fields.size();
            for (int i = 0, len = fieldCount; i < len; ++i) {
                byte[] field = fields.get(i);
                FieldPacket packet = new FieldPacket();
                packet.read(field);
                String column = new String(packet.name).toUpperCase();
                columnInfos.put(column, new ColumnInfo(i, packet.type));
                field[3] = ++packetId;
                buffer = front.writeToBuffer(field, buffer);
            }
            merger.init(columnInfos, fieldCount);
            eof[3] = ++packetId;
            buffer = front.writeToBuffer(eof, buffer);
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
            merger.offer(row);
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
            Iterator<RowDataPacket> itor = merger.getResult().iterator();
            while (itor.hasNext()) {
                RowDataPacket row = itor.next();
                itor.remove();
                row.packetId = ++packetId;
                buffer = row.write(buffer, front);
            }
            eof[3] = ++packetId;
            buffer = front.writeToBuffer(eof, allocBuffer());
            front.write(buffer);
        }
    }

    @Override
    public void close(BackendConnection con, String reason) {
        if (decrement()) {
            ErrorPacket err = new ErrorPacket();
            err.packetId = ++packetId;
            err.errno = ErrorCode.ER_YES;
            err.message = StringUtil.encode(reason, session.getFront().getCharset());
            err.write(session.getFront());
        }
    }

}
