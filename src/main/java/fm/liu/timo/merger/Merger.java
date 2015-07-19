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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fm.liu.timo.net.mysql.RowDataPacket;
import fm.liu.timo.route.Info;
import fm.liu.timo.route.Outlets;

/**
 * @author Liu Huanting 2015年6月4日
 */
public class Merger {
    private Grouper grouper;
    private Sorter sorter;
    private Distincter distincter;
    private ArrayDeque<RowDataPacket> rows;
    private Outlets outlets;
    private int fieldCount;

    public Merger(Outlets outlets) {
        this.outlets = outlets;
        this.rows = new ArrayDeque<RowDataPacket>();
    }

    public ArrayDeque<RowDataPacket> getResult() {
        if (grouper != null) {
            grouper.offer(rows);
            rows = grouper.getResult();
        }
        if (sorter != null) {
            sorter.offer(rows);
            rows = sorter.getResult();
        }
        if (distincter != null) {
            distincter.offer(rows);
            rows = distincter.getResult();
        }
        return rows;
    }

    public void init(Map<String, ColumnInfo> columnInfos, int fieldCount) {
        this.fieldCount = fieldCount;
        int info = outlets.getInfo();
        MergeInfo[] groupBy = null;
        ArrayList<MergeInfo> lsit = new ArrayList<MergeInfo>();
        if ((info & Info.HAS_GROUPBY) == Info.HAS_GROUPBY) {
            Set<String> columns = outlets.getGroupBy();
            groupBy = new MergeInfo[columns.size()];
            int i = 0;
            for (String column : columns) {
                groupBy[i++] = new MergeInfo(MergeType.ASC, columnInfos.get(column));
            }
        }
        if ((info & Info.NEED_MERGE) == Info.NEED_MERGE) {
            for (String column : columnInfos.keySet()) {
                int result = MergeInfo.getType(column);
                if (result != MergeType.UNSUPPORT) {
                    lsit.add(new MergeInfo(result, columnInfos.get(column)));
                }
            }
            this.grouper = new Grouper(groupBy, lsit.toArray(new MergeInfo[lsit.size()]));
        } else if ((info & Info.HAS_GROUPBY) == Info.HAS_GROUPBY) {
            Set<String> columns = outlets.getGroupBy();
            for (String column : columns) {
                lsit.add(new MergeInfo(MergeType.NOMERGE, columnInfos.get(column)));
            }
            this.grouper = new Grouper(groupBy, lsit.toArray(new MergeInfo[lsit.size()]));
        }
        if ((info & Info.HAS_ORDERBY) == Info.HAS_ORDERBY) {
            Map<String, Integer> orderBy = outlets.getOrderBy();
            MergeInfo[] mergeInfo = new MergeInfo[orderBy.size()];
            int i = 0;
            for (Entry<String, Integer> entry : orderBy.entrySet()) {
                mergeInfo[i++] = new MergeInfo(entry.getValue(), columnInfos.get(entry.getKey()));
            }
            this.sorter = new Sorter(mergeInfo);
        }
    }

    public void offer(byte[] row) {
        RowDataPacket packet = new RowDataPacket(fieldCount);
        packet.read(row);
        rows.add(packet);
    }

    public Outlets getOutlets() {
        return this.outlets;
    }
}
