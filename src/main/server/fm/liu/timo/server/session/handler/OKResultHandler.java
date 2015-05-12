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

import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.net.connection.BackendConnection;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public abstract class OKResultHandler implements ResultHandler{
    
    protected abstract void success(BackendConnection con);
    protected abstract void failed(BackendConnection con,String err);

    @Override
    public void ok(byte[] ok, BackendConnection con) {
        success(con);
    }

    @Override
    public void error(byte[] err, BackendConnection con) {
        String error = new String(err);
        failed(con,error);
        closeConnection(con,error);
    }

    @Override
    public void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con) {
        String error = "unexpected response";
        failed(con,error);
        closeConnection(con,error);
    }

    @Override
    public void row(byte[] row, BackendConnection con) {
        String error = "unexpected response";
        failed(con,error);
        closeConnection(con,error);
    }

    @Override
    public void eof(byte[] eof, BackendConnection con) {
        String error = "unexpected response";
        failed(con,error);
        closeConnection(con,error);
    }

    @Override
    public void close(BackendConnection con, String reason) {
        failed(con,reason);
    }

    private void closeConnection(BackendConnection con, String error) {
        if (con instanceof MySQLConnection) {
            ((MySQLConnection) con).setResultHandler(null);
        }
        con.close();
    }

}
