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

/**
 * @author Liu Huanting 2015年6月4日
 */
public class Distincter {
    private ArrayDeque<RowDataPacket> rows;

    public void offer(ArrayDeque<RowDataPacket> rows) {
        this.rows = rows;
    }

    public ArrayDeque<RowDataPacket> getResult() {
        ArrayDeque<RowDataPacket> temp = new ArrayDeque<RowDataPacket>();
        while (!rows.isEmpty()) {
            RowDataPacket row = rows.pollFirst();
            RowDataPacket nextRow = rows.peekFirst();
            while (!rows.isEmpty() && same(nextRow, row)) {
                rows.poll();
                nextRow = rows.peekFirst();
            }
            temp.add(row);
        }
        return temp;
    }

    private boolean same(RowDataPacket nextRow, RowDataPacket row) {
        int size = row.fieldCount;
        for (int i = 0; i < size; i++) {
            if (!Arrays.equals(nextRow.fieldValues.get(i), row.fieldValues.get(i))) {
                return false;
            }
        }
        return true;
    }
}
