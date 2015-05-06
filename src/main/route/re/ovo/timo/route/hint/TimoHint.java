/*
 * Copyright 1999-2012 Alibaba Group.
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
/**
 * (created at 2011-8-3)
 */
package re.ovo.timo.route.hint;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import re.ovo.timo.parser.util.Pair;
import re.ovo.timo.route.RouteResultsetNode;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public final class TimoHint {

    // index start from 1
    // /*!timo: $dataNodeId=0.0, $table='offer'*/
    // /*!timo: $dataNodeId=[0,1,5.2], $table='offer'*/
    // /*!timo: $partitionOperand=('member_id'='m1'), $table='offer'*/
    // /*!timo: $partitionOperand=('member_id'=['m1','m2']), $table='offer',
    // $replica=2*/
    // /*!timo: $partitionOperand=(['offer_id','group_id']=[123,'3c']),
    // $table='offer'*/
    // /*!timo:
    // $partitionOperand=(['offer_id','group_id']=[[123,'3c'],[234,'food']]),
    // $table='offer'*/

    public static final String Timo_HINT_PREFIX = "/*!timo:";
    private static final Map<String, HintParser> HINT_PARSERS = new HashMap<String, HintParser>();
    {
        HINT_PARSERS.put("table", new SimpleHintParser());
        HINT_PARSERS.put("replica", new SimpleHintParser());
        HINT_PARSERS.put("dataNodeId", new DataNodeHintParser());
        HINT_PARSERS.put("partitionOperand", new PartitionOperandHintParser());
    }

    private int replica = RouteResultsetNode.DEFAULT_REPLICA_INDEX;
    private String table;
    private List<Pair<Integer, Integer>> dataNodes;
    private Pair<String[], Object[][]> partitionOperand;

    /**
     * @param offset index of first char of {@link #Timo_HINT_PREFIX}
     */
    public static TimoHint parserTimoHint(String sql, int offset) throws SQLSyntaxErrorException {
        TimoHint hint = new TimoHint();
        hint.currentIndex = offset + Timo_HINT_PREFIX.length();
        hint.parse(sql);
        return hint;
    }

    /**
     * @return String[] in upper-case
     */
    public Pair<String[], Object[][]> getPartitionOperand() {
        return partitionOperand;
    }

    public void setPartitionOperand(Pair<String[], Object[][]> partitionOperand) {
        String[] columns = partitionOperand.getKey();
        if (columns == null) {
            this.partitionOperand = partitionOperand;
        } else {
            String[] colUp = new String[columns.length];
            for (int i = 0; i < columns.length; ++i) {
                colUp[i] = columns[i].toUpperCase();
            }
            this.partitionOperand =
                    new Pair<String[], Object[][]>(colUp, partitionOperand.getValue());
        }
    }

    public List<Pair<Integer, Integer>> getDataNodes() {
        return dataNodes;
    }

    public void addDataNode(Integer dataNodeIndex, Integer replica) {
        if (dataNodeIndex == null) {
            throw new IllegalArgumentException("data node index is null");
        }
        if (replica == RouteResultsetNode.DEFAULT_REPLICA_INDEX || replica < 0) {
            replica = null;
        }
        if (dataNodes == null) {
            dataNodes = new LinkedList<Pair<Integer, Integer>>();
        }
        dataNodes.add(new Pair<Integer, Integer>(dataNodeIndex, replica));
    }

    /**
     * @return upper case
     */
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table.toUpperCase();
    }

    public int getReplica() {
        return replica;
    }

    public void setReplica(int replica) {
        this.replica = replica;
    }

    private void parse(String sql) throws SQLSyntaxErrorException {
        timoHint: for (;;) {
            skip: for (;;) {
                switch (sql.charAt(currentIndex)) {
                    case '$':
                        break skip;
                    case '*':
                        currentIndex += 2;
                        break timoHint;
                    default:
                        ++currentIndex;
                }
            }
            int hintNameEnd = sql.indexOf('=', currentIndex);
            String hintName = sql.substring(currentIndex + 1, hintNameEnd).trim();
            HintParser hintParser = HINT_PARSERS.get(hintName);
            if (hintParser != null) {
                currentIndex = 1 + sql.indexOf('=', hintNameEnd);
                hintParser.process(this, hintName, sql);
            } else {
                throw new SQLSyntaxErrorException("unrecognized hint name: ${" + hintName + "}");
            }
        }
        outputSql = sql.substring(currentIndex);
    }

    private String outputSql;
    private int currentIndex;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public TimoHint increaseCurrentIndex() {
        ++currentIndex;
        return this;
    }

    public void setCurrentIndex(int ci) {
        currentIndex = ci;
    }

    public String getOutputSql() {
        return outputSql;
    }
}
