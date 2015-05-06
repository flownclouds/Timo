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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import re.ovo.timo.config.model.ClusterConfig;
import re.ovo.timo.config.model.TimoNodeConfig;

/**
 * @author haiqing.zhuhq 2012-3-21
 */
public final class TimoCluster {

    private final Map<String, TimoNode> nodes;
    private final Map<String, List<String>> groups;

    public TimoCluster(ClusterConfig clusterConf) {
        this.nodes = new HashMap<String, TimoNode>(clusterConf.getNodes().size());
        this.groups = clusterConf.getGroups();
        for (TimoNodeConfig conf : clusterConf.getNodes().values()) {
            String name = conf.getName();
            TimoNode node = new TimoNode(conf);
            this.nodes.put(name, node);
        }
    }

    public Map<String, TimoNode> getNodes() {
        return nodes;
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

}
