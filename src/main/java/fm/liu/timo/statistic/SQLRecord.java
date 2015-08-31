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
package fm.liu.timo.statistic;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xianmao.hexm
 */
public final class SQLRecord implements Comparable<SQLRecord> {

    public final String     host;
    public final String     schema;
    public final String     statement;
    public final long       startTime;
    public final long       executeTime;
    public final int        datanode;
    public final AtomicLong count = new AtomicLong();

    public SQLRecord(String host, String schema, String statement, long startTime, long executeTime,
            int datanode) {

        this.host = host;
        this.schema = schema;
        this.statement = statement;
        this.startTime = startTime;
        this.executeTime = executeTime;
        this.datanode = datanode;
    }

    @Override
    public int compareTo(SQLRecord o) {
        return (int) (executeTime - o.executeTime);
    }

}
