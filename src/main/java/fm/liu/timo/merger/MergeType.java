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
package fm.liu.timo.merger;

/**
 * @author Liu Huanting 2015年6月4日
 */
public class MergeType {
    public static final int COUNT     = 1;
    public static final int SUM       = 2;
    public static final int MIN       = 3;
    public static final int MAX       = 4;
    public static final int UNSUPPORT = -1;
    public static final int NOMERGE   = -2;
    public static final int ASC       = 5;
    public static final int DESC      = 6;
}
