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
package fm.liu.timo.net.backend;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import fm.liu.timo.config.model.Datasource.Type;
import fm.liu.timo.util.TimeUtil;

/**
 * @author Liu Huanting 2015年5月9日
 */
public class Node {
    private final int                              id;
    private final Map<Integer, Source>             sources;
    private final Map<Integer, ArrayList<Integer>> handovers;
    private final ReentrantLock                    lock = new ReentrantLock();
    private volatile Source                        source;
    private volatile long                          heartbeatRecoveryTime;

    public Node(int id, Map<Integer, Source> sources, Map<Integer, ArrayList<Integer>> handovers) {
        this.id = id;
        this.sources = sources;
        this.handovers = handovers;
    }

    public boolean init() {
        boolean chosen = false;
        for (Source source : sources.values()) {
            if (!source.isAvailable()) {
                continue;
            }
            if (!source.init()) {
                return false;
            }
            Type type = source.getConfig().getType();
            if (!chosen && Type.MASTER.equals(type)) {
                this.source = source;
                chosen = true;
            } else if (!chosen && Type.BACKUP.equals(type)) {
                this.source = source;
                chosen = true;
            } else if (!chosen && Type.SLAVE.equals(type)) {
                this.source = source;
                chosen = true;
            }
        }
        return chosen;
    }

    public void idleCheck() {
        for (Source source : sources.values()) {
            if (source != null && source.isAvailable()) {
                source.idleCheck();
            }
        }
    }

    public void heartbeat() {
        if (TimeUtil.currentTimeMillis() < heartbeatRecoveryTime) {
            for (Source source : sources.values()) {
                if (source != null && source.isAvailable()) {
                    source.getHeartbeat().pause();
                }
            }
            return;
        }
        for (Source source : sources.values()) {
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

    public Map<Integer, Source> getSources() {
        return sources;
    }

    public void setHeartbeatRecoveryTime(long heartbeatRecoveryTime) {
        this.heartbeatRecoveryTime = heartbeatRecoveryTime;
    }

    public void handover(int id) throws Exception {
        ArrayList<Integer> handover = handovers.get(id);
        if (handover == null) {
            throw new Exception("cann't switch source " + id + " without handover infomation");
        }
        lock.lock();
        try {
            for (Integer standby : handover) {
                Source ds = sources.get(standby);
                if (ds.isAvailable()) {
                    source = ds;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
