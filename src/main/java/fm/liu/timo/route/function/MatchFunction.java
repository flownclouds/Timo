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
package fm.liu.timo.route.function;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import fm.liu.timo.config.model.Function;

/**
 * @author Liu Huanting 2015年5月10日
 * <pre>
 * 匹配路由
 * 未匹配到的值路由到默认节点
 * </pre>
 */
public class MatchFunction implements Function {
    private final Map<Object, Integer> mapping;
    private final int                  defaultNode;

    public MatchFunction(Map<Object, Integer> mapping, int defaultNode) {
        this.mapping = mapping;
        this.defaultNode = defaultNode;
    }

    @Override
    public Set<Integer> calcute(Collection<Object> values) {
        Set<Integer> result = new HashSet<Integer>();
        for (Object value : values) {
            if (mapping.containsKey(value)) {
                result.add(mapping.get(value));
            } else {
                result.add(defaultNode);
            }
        }
        return result;
    }

}
