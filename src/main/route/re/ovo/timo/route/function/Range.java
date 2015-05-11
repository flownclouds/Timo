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

/**
 * @author Liu Huanting
 * 2015年5月10日
 */
public class Range {
    private final int node;
    private final long min;
    private final long max;
    
    public Range(int node,long min,long max) {
        this.node = node;
        this.min = min;
        this.max = max;
    }

    public int getNode() {
        return node;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }
}
