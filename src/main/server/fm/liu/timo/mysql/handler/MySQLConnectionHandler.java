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
package fm.liu.timo.mysql.handler;

import java.util.ArrayList;
import java.util.List;

import fm.liu.timo.mysql.ByteUtil;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.net.connection.AbstractConnection.State;
import fm.liu.timo.net.handler.BackendHandler;
import fm.liu.timo.net.mysql.EOFPacket;
import fm.liu.timo.net.mysql.ErrorPacket;
import fm.liu.timo.net.mysql.OkPacket;
import fm.liu.timo.server.session.handler.ResultHandler;

/**
 * life cycle: from connection establish to close <br/>
 * 
 * @author xianmao.hexm 2012-4-12
 */
public class MySQLConnectionHandler extends BackendHandler {
    private static final int RESULT_STATUS_INIT = 0;
    private static final int RESULT_STATUS_HEADER = 1;
    private static final int RESULT_STATUS_FIELD_EOF = 2;

    private final MySQLConnection con;
    private volatile int resultStatus;
    private volatile byte[] header;
    private volatile List<byte[]> fields;

    public MySQLConnectionHandler(MySQLConnection source) {
        this.con = source;
        this.resultStatus = RESULT_STATUS_INIT;
    }

    public void connectionError(Throwable e) {
        dataQueue.clear();
        ResultHandler handler = con.getResultHandler();
        if(handler!=null){
            handler.close(con, "connection error");
        }
    }

    public MySQLConnection getSource() {
        return con;
    }

    @Override
    public void handle(byte[] data) {
        offerData(data, con.getProcessor().getExecutor());
    }

    @Override
    protected void offerDataError() {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        throw new RuntimeException("offer data error!");
    }

    @Override
    protected void handleData(byte[] data) {
        switch (resultStatus) {
            case RESULT_STATUS_INIT:
                switch (data[4]) {
                    case OkPacket.FIELD_COUNT:
                        handleOkPacket(data);
                        break;
                    case ErrorPacket.FIELD_COUNT:
                        handleErrorPacket(data);
                        break;
                    default:
                        resultStatus = RESULT_STATUS_HEADER;
                        header = data;
                        fields = new ArrayList<byte[]>((int) ByteUtil.readLength(data, 4));
                }
                break;
            case RESULT_STATUS_HEADER:
                switch (data[4]) {
                    case ErrorPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_INIT;
                        handleErrorPacket(data);
                        break;
                    case EOFPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_FIELD_EOF;
                        handleFieldEofPacket(data);
                        break;
                    default:
                        fields.add(data);
                }
                break;
            case RESULT_STATUS_FIELD_EOF:
                switch (data[4]) {
                    case ErrorPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_INIT;
                        handleErrorPacket(data);
                        break;
                    case EOFPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_INIT;
                        handleRowEofPacket(data);
                        break;
                    default:
                        handleRowPacket(data);
                }
                break;
            default:
                throw new RuntimeException("unknown status!");
        }
    }

    @Override
    protected void handleDataError(Throwable t) {
        dataQueue.clear();
        resultStatus = RESULT_STATUS_INIT;
        ResultHandler handler = con.getResultHandler();
        if(handler!=null){
            handler.close(con, "handle data error");
        }
    }

    /**
     * OK数据包处理
     */
    private void handleOkPacket(byte[] data) {
        con.setState(State.borrowed);
        ResultHandler handler = con.getResultHandler();
        if (handler != null) {
            handler.ok(data, con);
        }
    }

    /**
     * ERROR数据包处理
     */
    private void handleErrorPacket(byte[] data) {
        con.setState(State.borrowed);
        ResultHandler handler = con.getResultHandler();
        if (handler != null) {
            handler.error(data, con);
        }
    }

    /**
     * 字段数据包结束处理
     */
    private void handleFieldEofPacket(byte[] data) {
        ResultHandler handler = con.getResultHandler();
        if (handler != null) {
            handler.field(header, fields, data, con);
        }
    }

    /**
     * 行数据包处理
     */
    private void handleRowPacket(byte[] data) {
        ResultHandler handler = con.getResultHandler();
        if (handler != null) {
            handler.row(data, con);
        }
    }

    /**
     * 行数据包结束处理
     */
    private void handleRowEofPacket(byte[] data) {
        con.setState(State.borrowed);
        ResultHandler handler = con.getResultHandler();
        if (handler != null) {
            handler.eof(data, con);
        }
    }

}
