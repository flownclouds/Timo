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
package re.ovo.timo;

import org.apache.log4j.Logger;

import re.ovo.timo.config.model.TimoNodeConfig;
import re.ovo.timo.heartbeat.TimoHeartbeat;

/**
 * @author xianmao.hexm
 */
public class TimoNode {
    private static final Logger LOGGER = Logger.getLogger(TimoNode.class);

    private final String name;
    private final TimoNodeConfig config;
    private final TimoHeartbeat heartbeat;

    public TimoNode(TimoNodeConfig config) {
        this.name = config.getName();
        this.config = config;
        this.heartbeat = new TimoHeartbeat(this);
    }

    public String getName() {
        return name;
    }

    public TimoNodeConfig getConfig() {
        return config;
    }

    public TimoHeartbeat getHeartbeat() {
        return heartbeat;
    }

    public void stopHeartbeat() {
        heartbeat.stop();
    }

    public void startHeartbeat() {
        heartbeat.start();
    }

    public void doHeartbeat() {
        if (!heartbeat.isStop()) {
            try {
                heartbeat.heartbeat();
            } catch (Throwable e) {
                LOGGER.error(name + " heartbeat error.", e);
            }
        }
    }

    public boolean isOnline() {
        return (heartbeat.getStatus() == TimoHeartbeat.OK_STATUS);
    }

}
