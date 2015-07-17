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
 */
public class HashFunction implements Function {
    private final static int size = 1024;
    private final Set<Range> ranges;

    public HashFunction(Set<Range> ranges) {
        this.ranges = ranges;
    }

    @Override
    public Set<Integer> calcute(Collection<Object> values) {
        Set<Integer> result = new HashSet<Integer>();
        for (Object value : values) {
            long val = StringUtil.hash(String.valueOf(value)) % size;
            for (Range range : ranges) {
                if (range.getMin() <= val && val <= range.getMax()) {
                    result.add(range.getNode());
                }
            }
        }
        return result;
    }

}
