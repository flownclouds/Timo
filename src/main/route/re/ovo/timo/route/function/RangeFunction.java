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
package re.ovo.timo.route.function;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import re.ovo.timo.config.model.Function;

/**
 * @author Liu Huanting
 * 2015年5月10日
 */
public class RangeFunction implements Function{

    private final Set<Range> ranges;
    private final int defaultNode;

    public RangeFunction(Set<Range> ranges,int defaultNode) {
        this.ranges = ranges;
        this.defaultNode = defaultNode;
    }

    @Override
    public Set<Integer> calcute(Collection<Object> values) {
        Set<Integer> result = new HashSet<Integer>();
        for (Object value : values) {
            long val = Long.parseLong(String.valueOf(value));
            for (Range range : ranges) {
                if (range.getMin() <= val && val <= range.getMax()) {
                    result.add(range.getNode());
                }
            }
        }
        if(result.isEmpty()){
            result.add(defaultNode);
        }
        return result;
    }
}
