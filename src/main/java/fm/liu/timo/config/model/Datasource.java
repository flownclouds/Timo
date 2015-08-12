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
package fm.liu.timo.config.model;

/**
 * @author Liu Huanting 2015年5月9日
 */
public class Datasource {
    private final int id;
    private int       datanodeID;
    private String    host;
    private int       port;
    private String    username;
    private String    password;
    private String    db;
    private Type      type;
    private Status    status;
    private String    charset;
    private int       initCon;
    private int       maxCon;
    private int       minIdle;
    private int       maxIdle;
    private long      idleCheckPeriod;

    public enum Type {
        MASTER, BACKUP, SLAVE
    }
    public enum Status {
        NORMAL, ERROR
    }

    public Datasource(int id, int datanodeID, String host, int port, String username,
            String password, String db, int type, int status, String charset, int initCon,
            int maxCon, int minIdle, int maxIdle, int idleCheckPeriod) {
        this.id = id;
        this.datanodeID = datanodeID;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.db = db;
        switch (type) {
            case 1:
                this.type = Type.MASTER;
                break;
            case 2:
                this.type = Type.BACKUP;
                break;
            default:
                this.type = Type.SLAVE;
        }
        switch (status) {
            case 1:
                this.status = Status.NORMAL;
                break;
            default:
                this.status = Status.ERROR;
        }
        this.charset = charset;
        this.initCon = initCon;
        this.maxCon = maxCon;
        this.minIdle = minIdle;
        this.maxIdle = maxIdle;
        this.idleCheckPeriod = idleCheckPeriod * 1000L;
    }

    public int getID() {
        return id;
    }

    public int getDatanodeID() {
        return datanodeID;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDB() {
        return db;
    }

    public Type getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public String getCharset() {
        return charset;
    }

    public int getInitCon() {
        return initCon;
    }

    public int getMaxCon() {
        return maxCon;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public long getIdleCheckPeriod() {
        return idleCheckPeriod;
    }
}
