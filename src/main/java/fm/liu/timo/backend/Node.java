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
package fm.liu.timo.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.model.Datanode.Strategy;
import fm.liu.timo.server.session.handler.ResultHandler;
import fm.liu.timo.util.TimeUtil;

/**
 * 数据节点
 * @author Liu Huanting 2015年5月9日
 */
public class Node {
    private final int           id;
    private final Strategy      strategy;
    private final List<Source>  sources;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile Source     source;
    private volatile long       heartbeatRecoveryTime;

    public Node(int id, Strategy strategy, List<Source> sources) {
        this.id = id;
        this.strategy = strategy;
        this.sources = sources;
    }

    public boolean init() {
        boolean chosen = false;
        for (Source source : sources) {
            if (!source.isAvailable()) {
                continue;
            }
            if (!source.init()) {
                Logger.warn("source {} init failed.", source.getConfig());
                return false;
            }
            if (!chosen) {
                this.source = source;
                chosen = true;
            }
        }
        return chosen;
    }

    public void idleCheck() {
        for (Source source : sources) {
            if (source != null && source.isAvailable()) {
                source.idleCheck();
            }
        }
    }

    public void heartbeat() {
        if (TimeUtil.currentTimeMillis() < heartbeatRecoveryTime) {
            for (Source source : sources) {
                if (source != null && source.isAvailable()) {
                    source.getHeartbeat().pause();
                }
            }
            return;
        }
        for (Source source : sources) {
            if (source != null && source.isAvailable()) {
                source.heartbeat(this);
            }
        }
    }

    public int getID() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setHeartbeatRecoveryTime(long heartbeatRecoveryTime) {
        this.heartbeatRecoveryTime = heartbeatRecoveryTime;
    }

    public boolean handover(boolean manual) throws Exception {
        boolean success = false;
        ArrayList<Source> backups = source.getBackups();
        if (backups == null || backups.isEmpty()) {
            throw new Exception("cann't handover source without backup infomation");
        }
        lock.lock();
        try {
            for (Source source : backups) {
                if (source.isAvailable()) {
                    if (!manual) {
                        this.source.getConfig().ban();
                        this.source.clear("datanode handover by manager");
                    } else {
                        Logger.info("handover datanode {} to datasource {} by manager.",
                                this.getID(), source);
                    }
                    this.source = source;
                    success = true;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return success;
    }

    public void clear(String reason) {
        for (Source source : sources) {
            source.clear(reason);
        }
    }

    public void query(String sql, ResultHandler handler, boolean read) {
        Source source = this.source;
        switch (strategy) {
            case MRW_SR:
                if (read) {
                    source = randomAll();
                }
                break;
            case MW_SR:
                if (read) {
                    source = randomRead();
                }
                break;
            default:
                break;
        }
        if (!source.isAvailable()) {
            source = this.source;
        }
        source.query(sql, handler);
    }

    private Source randomRead() {
        ArrayList<Source> backups = source.getBackups();
        if (backups == null || backups.isEmpty()) {
            return this.source;
        }
        return backups.get(ThreadLocalRandom.current().nextInt(backups.size()));
    }

    private Source randomAll() {
        ArrayList<Source> backups = source.getBackups();
        if (backups == null || backups.isEmpty()) {
            return this.source;
        }
        int seed = ThreadLocalRandom.current().nextInt(backups.size() + 1);
        switch (seed) {
            case 0:
                return this.source;
            default:
                return source.getBackups().get(seed - 1);
        }
    }

    public Strategy getStrategy() {
        return strategy;
    }

}
