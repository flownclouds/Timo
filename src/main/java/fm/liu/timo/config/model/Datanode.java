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

import java.util.List;

/**
 * @author Liu Huanting 2015年5月9日
 * 数据节点配置信息
 */
public class Datanode {
    private final int           id;
    private final Strategy      strategy;
    private final List<Integer> datasources;

    /**
     * <pre>
     * MRW--------仅主节点参与读写(默认)
     * MRW_SR-----主节点参与读写，从节点只读
     * MW_SR------主节点只写，从节点只读
     * </pre>
     * @author liuhuanting
     */
    public enum Strategy {
        MRW, MRW_SR, MW_SR
    }

    public Datanode(int id, int strategy, List<Integer> datasources) {
        this.id = id;
        switch (strategy) {
            case 1:
                this.strategy = Strategy.MRW_SR;
                break;
            case 2:
                this.strategy = Strategy.MW_SR;
                break;
            default:
                this.strategy = Strategy.MRW;
        }
        this.datasources = datasources;
    }

    public int getID() {
        return id;
    }

    public List<Integer> getDatasources() {
        return datasources;
    }

    public Strategy getStrategy() {
        return strategy;
    }
}
