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
package fm.liu.timo.net.handler;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.ErrorCode;
import fm.liu.timo.mysql.SecurityUtil;
import fm.liu.timo.net.NIOHandler;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.net.mysql.AuthPacket;
import fm.liu.timo.net.mysql.CommandPacket;
import fm.liu.timo.net.mysql.QuitPacket;

/**
 * 前端认证处理器
 * 
 * @author xianmao.hexm
 */
public class FrontendAuthenticator implements NIOHandler {
    private static final byte[] AUTH_OK = new byte[] {7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0};

    protected final FrontendConnection source;

    public FrontendAuthenticator(FrontendConnection source) {
        this.source = source;
    }

    @Override
    public void handle(byte[] data) {
        // check quit packet
        if (data.length == QuitPacket.QUIT.length && data[4] == CommandPacket.COM_QUIT) {
            source.close("quit");
            return;
        }

        AuthPacket auth = new AuthPacket();
        auth.read(data);

        // check user
        if (!checkUser(auth.user, source.getHost())) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + auth.user + "'");
            return;
        }

        // check password
        if (!checkPassword(auth.password, auth.user)) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + auth.user + "'");
            return;
        }

        // check schema
        switch (checkSchema(auth.database, auth.user)) {
            case ErrorCode.ER_BAD_DB_ERROR:
                failure(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + auth.database + "'");
                break;
            case ErrorCode.ER_DBACCESS_DENIED_ERROR:
                String s = "Access denied for user '" + auth.user + "' to database '"
                        + auth.database + "'";
                failure(ErrorCode.ER_DBACCESS_DENIED_ERROR, s);
                break;
            default:
                success(auth);
        }
    }

    protected boolean checkUser(String user, String host) {
        return source.getPrivileges().userExists(user, host);
    }

    protected boolean checkPassword(byte[] password, String user) {
        String pass = source.getPrivileges().getPassword(user);

        // check null
        if (pass == null || pass.length() == 0) {
            if (password == null || password.length == 0) {
                return true;
            } else {
                return false;
            }
        }
        if (password == null || password.length == 0) {
            return false;
        }

        // encrypt
        byte[] encryptPass = null;
        try {
            encryptPass = SecurityUtil.scramble411(pass.getBytes(), source.getSeed());
        } catch (NoSuchAlgorithmException e) {
            Logger.warn("FrontConnection:{} Exception:{}", source.toString(), e);
            return false;
        }
        if (encryptPass != null && (encryptPass.length == password.length)) {
            int i = encryptPass.length;
            while (i-- != 0) {
                if (encryptPass[i] != password[i]) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    protected int checkSchema(String schema, String user) {
        if (schema == null) {
            return 0;
        }
        FrontendPrivileges privileges = source.getPrivileges();
        if (!privileges.schemaExists(schema)) {
            return ErrorCode.ER_BAD_DB_ERROR;
        }
        Set<String> schemas = privileges.getUserSchemas(user);
        if (schemas == null || schemas.size() == 0 || schemas.contains(schema)) {
            return 0;
        } else {
            return ErrorCode.ER_DBACCESS_DENIED_ERROR;
        }
    }

    protected void success(AuthPacket auth) {
        source.setAuthenticated(true);
        source.setUser(auth.user);
        source.setDB(auth.database);
        source.getVariables().setCharsetIndex(auth.charsetIndex);
        source.setHandler(new FrontendCommandHandler(source));
        if (Logger.isDebugEnabled()) {
            Logger.debug("Login success for {}/{}", source, auth.user);
        }
        ByteBuffer buffer = source.allocate();
        source.write(source.writeToBuffer(AUTH_OK, buffer));
    }

    protected void failure(int errno, String info) {
        Logger.warn("Login failure for {} by {}", source, info);
        source.writeErrMessage((byte) 2, errno, info);
    }

}
