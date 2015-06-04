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

import java.util.ArrayDeque;
import java.util.Arrays;

import fm.liu.timo.net.mysql.RowDataPacket;
import fm.liu.timo.util.ByteUtil;
import fm.liu.timo.util.CompareUtil;
import fm.liu.timo.util.LongUtil;

/**
 * @author Liu Huanting 2015年6月4日
 */
public class Grouper {
    private final MergeInfo[] mergeInfo;
    private ArrayDeque<RowDataPacket> rows;
    private Sorter sorter;

    public Grouper(MergeInfo[] groupBy,MergeInfo[] mergeInfo) {
        this.mergeInfo = mergeInfo;
        if(groupBy!=null){
            this.sorter = new Sorter(groupBy);
        }
    }

    public void offer(ArrayDeque<RowDataPacket> rows) {
        if(sorter!=null){
            sorter.offer(rows);
        }else{
            this.rows = rows;
        }
    }

    public ArrayDeque<RowDataPacket> getResult() {
        if(sorter!=null){
            
            rows = sorter.getResult();
        }
        ArrayDeque<RowDataPacket> tmpResult = new ArrayDeque<RowDataPacket>();
        while (!rows.isEmpty()) {
            RowDataPacket row = rows.pollFirst();
            RowDataPacket nextRow = rows.peekFirst();
            while (!rows.isEmpty() && same(nextRow, row)) {
                merge(row, nextRow);
                rows.poll();
                nextRow = rows.peekFirst();
            }
            tmpResult.add(row);
        }
        return tmpResult;
    }

    private boolean same(RowDataPacket nextRow, RowDataPacket row) {
        if (sorter == null) {
            return true;
        }
        int length = mergeInfo.length;
        for (int i = 0; i < length; i++) {
            int index = mergeInfo[i].columnInfo.index;
            if (!Arrays.equals(nextRow.fieldValues.get(index), row.fieldValues.get(index))) {
                return false;
            }
        }
        return true;
    }

    private void merge(RowDataPacket row, RowDataPacket nextRow) {
        if (mergeInfo == null) {
            return;
        }
        for (MergeInfo column : mergeInfo) {
            int index = column.columnInfo.index;
            byte[] result =
                    merge(row.fieldValues.get(index), nextRow.fieldValues.get(index),
                            column.columnInfo.type, column.type);
            if (result != null) {
                row.fieldValues.set(index, result);
            }
        }
    }

    private byte[] merge(byte[] value, byte[] nextValue, int valueType, int mergeType) {
        if (value.length == 0) {
            return nextValue;
        } else if (nextValue.length == 0) {
            return value;
        }
        switch (mergeType) {
            case MergeType.MAX:
                return CompareUtil.compareBytes(value, nextValue) > 0 ? value : nextValue;
            case MergeType.MIN:
                return CompareUtil.compareBytes(value, nextValue) > 0 ? nextValue : value;
            case MergeType.SUM:
                switch (valueType) {
                    case ColumnType.NEWDECIMAL:
                    case ColumnType.DOUBLE:
                    case ColumnType.FLOAT:
                        Double val = ByteUtil.getDouble(value) + ByteUtil.getDouble(nextValue);
                        return val.toString().getBytes();
                }
            case MergeType.COUNT:
                return LongUtil.toBytes(ByteUtil.getLong(value) + ByteUtil.getLong(nextValue));
        }
        return null;
    }
}
