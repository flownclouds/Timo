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
 * 聚合信息
 * 
 * @author Liu Huanting 2015年6月4日
 */
public class MergeInfo {
    public final int type;
    public final ColumnInfo columnInfo;

    public MergeInfo(int type, ColumnInfo columnInfo) {
        this.type = type;
        this.columnInfo = columnInfo;
    }

    public static int getType(String column) {
        if (column.length() < 5) {
            return MergeType.UNSUPPORT;
        }
        column = column.toUpperCase();
        if (column.startsWith("MAX")) {
            return MergeType.MAX;
        } else if (column.startsWith("MIN")) {
            return MergeType.MIN;
        } else if (column.startsWith("SUM")) {
            return MergeType.SUM;
        } else if (column.startsWith("COUNT")) {
            return MergeType.COUNT;
        } else {
            return MergeType.UNSUPPORT;
        }
    }
}
