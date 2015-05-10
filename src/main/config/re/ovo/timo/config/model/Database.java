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
package re.ovo.timo.config.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class Database {
    private final int id;
    private final String name;
    private final Map<String, Table> tables;
    private final List<Integer> nodes;

    public Database(int id, String name, Map<String, Table> tables) {
        this.id = id;
        this.name = name;
        this.tables = tables;
        this.nodes = configNodes(tables.values());
    }

    private List<Integer> configNodes(Collection<Table> tables) {
        List<Integer> nodes = new ArrayList<Integer>();
        for (Table table : tables) {
            for (Integer id : table.getNodes()) {
                if (!nodes.contains(id)) {
                    nodes.add(id);
                }
            }
        }
        return nodes;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public int getRandomNode() {
        return nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
    }

}
