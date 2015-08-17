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
import java.util.Set;
import fm.liu.timo.config.model.Function;
import fm.liu.timo.util.StringUtil;

/**
 * @author Liu Huanting 2015年5月10日
 * <pre>
 * 自动哈希函数
 * 根据节点数取余后路由
 * </pre>
 */
public class AutoFunction implements Function {
    private int size;

    public AutoFunction(int size) {
        this.size = size;
    }

    @Override
    public Set<Integer> calcute(Collection<Object> values) {
        Set<Integer> result = new HashSet<Integer>();
        for (Object value : values) {
            result.add((int) (StringUtil.hash(String.valueOf(value)) % size + 1));
        }
        return result;
    }

}
