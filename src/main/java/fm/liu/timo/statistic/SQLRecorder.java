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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import fm.liu.messenger.Mail;
import fm.liu.messenger.User;

/**
 * SQL统计排序记录器
 * 
 * @author xianmao.hexm 2010-9-30 上午10:48:28
 */
public final class SQLRecorder extends User {
    private volatile ConcurrentHashMap<String, SQLRecord> records;
    private final ReentrantLock                           lock;

    public SQLRecorder() {
        this.records = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
    }

    public List<SQLRecord> getRecords() {
        List<SQLRecord> results = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Map.Entry<String, SQLRecord>> entryList =
                    new ArrayList<Map.Entry<String, SQLRecord>>(records.entrySet());
            Collections.sort(entryList, new Comparator<Map.Entry<String, SQLRecord>>() {
                public int compare(Entry<String, SQLRecord> entry1,
                        Entry<String, SQLRecord> entry2) {
                    // 倒序
                    return entry2.getValue().compareTo(entry1.getValue());
                }
            });
            Iterator<Map.Entry<String, SQLRecord>> iter = entryList.iterator();
            while (iter.hasNext()) {
                results.add(iter.next().getValue());
            }
        }
        return results;
    }

    @Override
    public void receive(Mail<?> mail) {
        SQLRecord record = (SQLRecord) mail.msg;
        String sql = record.statement;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (records.containsKey(sql)) {
                SQLRecord _records = records.get(sql);
                if (_records.compareTo(record) > 0) {
                    record.count.set(_records.count.incrementAndGet());
                    records.put(sql, record);
                } else {
                    _records.count.incrementAndGet();
                }
            } else {
                record.count.incrementAndGet();
                records.put(sql, record);
            }
        } finally {
            lock.unlock();
        }
    }

}
