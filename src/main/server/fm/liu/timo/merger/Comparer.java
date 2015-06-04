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

import fm.liu.timo.util.CompareUtil;

/**
 * 行数据比较器
 * 
 * @author Liu Huanting 2015年6月4日
 */
public class Comparer {
    public static int compareColumn(byte[] base, byte[] column, int colType) {
        switch (colType) {
            case ColumnType.DECIMAL:
            case ColumnType.INT:
            case ColumnType.SHORT:
            case ColumnType.LONG:
            case ColumnType.FLOAT:
            case ColumnType.DOUBLE:
            case ColumnType.LONGLONG:
            case ColumnType.INT24:
            case ColumnType.NEWDECIMAL:
                // 因为mysql的日期也是数字字符串方式表达，因此可以跟整数等一起对待
            case ColumnType.DATE:
            case ColumnType.TIMSTAMP:
            case ColumnType.TIME:
            case ColumnType.YEAR:
            case ColumnType.DATETIME:
            case ColumnType.NEWDATE:
            case ColumnType.BIT:
                return CompareUtil.compareBytes(base, column);
            case ColumnType.VAR_STRING:
            case ColumnType.STRING:
                // ENUM和SET类型都是字符串，按字符串处理
            case ColumnType.ENUM:
            case ColumnType.SET:
                return CompareUtil.compareString(new String(base), new String(column));
                // BLOB相关类型和GEOMETRY类型不支持排序，略掉
        }
        return 0;
    }
}
