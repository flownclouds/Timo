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
package re.ovo.timo.net.backend;

import java.util.Map;

import re.ovo.timo.config.model.Datasource.Type;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class Node {
    private final int id;
    private volatile Source source;
    private final Map<Integer,Source> sources;
    public Node(int id, Map<Integer,Source> sources) {
        this.id = id;
        this.sources = sources;
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
    
    public int getID(){
        return id;
    }
    
    public Source getSource(){
        return source;
    }
    
    public Map<Integer, Source> getSources(){
        return sources;
    }
}
