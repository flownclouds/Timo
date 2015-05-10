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

import java.util.List;

/**
 * @author Liu Huanting
 * 2015年5月9日
 */
public class Datanode {
    private final int id;
    private final List<Integer> datasources;

    public Datanode(int id, List<Integer> datasources) {
        this.id = id;
        this.datasources = datasources;
    }

    public int getID() {
        return id;
    }

    public List<Integer> getDatasources() {
        return datasources;
    }
}
