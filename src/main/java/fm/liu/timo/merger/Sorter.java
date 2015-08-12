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
import java.util.Collections;
import fm.liu.timo.net.mysql.RowDataPacket;

/**
 * @author Liu Huanting 2015年6月4日
 */
public class Sorter {
    private final MergeInfo[]         mergeInfo;
    private ArrayDeque<RowDataPacket> rows;
    private RowDataPacket[]           result;
    private RowDataPacket[]           temp;
    private int                       p;
    private int                       pL;
    private int                       pR;

    public Sorter(MergeInfo[] mergeInfo) {
        this.mergeInfo = mergeInfo;
    }

    public void offer(ArrayDeque<RowDataPacket> rows) {
        this.rows = rows;
    }

    public ArrayDeque<RowDataPacket> getResult() {
        sort(rows.toArray(new RowDataPacket[rows.size()]));
        if (result != null) {
            Collections.addAll(rows, result);
        }
        return rows;
    }

    private RowDataPacket[] sort(RowDataPacket[] rows) {
        this.rows.clear();
        result = rows;
        if (rows == null || rows.length < 2 || this.mergeInfo == null
                || this.mergeInfo.length < 1) {
            return rows;
        }
        merge(0, rows.length - 1);
        return result;
    }

    private void merge(int l, int r) {
        if (l < r) {
            int mid = (l + r) / 2;
            merge(l, mid);
            merge(mid + 1, r);
            merge(l, mid, r);
        }
    }

    private void merge(int l, int mid, int r) {
        temp = new RowDataPacket[(r - l) + 1];
        p = 0;
        pL = l;
        pR = mid + 1;
        while (pL <= mid || pR <= r) {
            if (pL == mid + 1) {
                while (pR <= r) {
                    temp[p++] = result[pR++];
                }
            } else if (pR == r + 1) {
                while (pL <= mid) {
                    temp[p++] = result[pL++];
                }
            } else {
                compare(0);
            }
        }
        for (pL = l, pR = 0; pL <= r; pL++, pR++) {
            result[pL] = temp[pR];
        }
    }

    private void compare(int index) {
        if (index == this.mergeInfo.length) {
            if (this.mergeInfo[index - 1].type == MergeType.ASC) {
                temp[p++] = result[pL++];
            } else {
                temp[p++] = result[pR++];
            }
            return;
        }
        ColumnInfo info = this.mergeInfo[index].columnInfo;
        byte[] left = result[pL].fieldValues.get(info.index);
        byte[] right = result[pR].fieldValues.get(info.index);
        int r = Comparer.compareColumn(left, right, info.type);
        if (r <= 0) {
            if (r < 0) {
                if (this.mergeInfo[index].type == MergeType.ASC) {
                    temp[p++] = result[pL++];
                } else {
                    temp[p++] = result[pR++];
                }
            } else {
                compare(index + 1);
            }
        } else {
            if (this.mergeInfo[index].type == MergeType.ASC) {
                temp[p++] = result[pR++];
            } else {
                temp[p++] = result[pL++];
            }
        }
    }
}
