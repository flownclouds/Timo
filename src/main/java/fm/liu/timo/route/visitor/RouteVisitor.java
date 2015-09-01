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
package fm.liu.timo.route.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.config.model.Table;
import fm.liu.timo.config.model.Table.TableType;
import fm.liu.timo.merger.MergeType;
import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.PolyadicOperatorExpression;
import fm.liu.timo.parser.ast.expression.comparison.ComparisionEqualsExpression;
import fm.liu.timo.parser.ast.expression.logical.LogicalOrExpression;
import fm.liu.timo.parser.ast.expression.primary.Identifier;
import fm.liu.timo.parser.ast.expression.primary.RowExpression;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Count;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Max;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Min;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Sum;
import fm.liu.timo.parser.ast.fragment.GroupBy;
import fm.liu.timo.parser.ast.fragment.Limit;
import fm.liu.timo.parser.ast.fragment.OrderBy;
import fm.liu.timo.parser.ast.fragment.SortOrder;
import fm.liu.timo.parser.ast.fragment.tableref.TableRefFactor;
import fm.liu.timo.parser.ast.stmt.ddl.DDLCreateTableStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLDropTableStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLInsertReplaceStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLInsertStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLReplaceStatement;
import fm.liu.timo.parser.util.Pair;
import fm.liu.timo.parser.visitor.Visitor;
import fm.liu.timo.route.Info;

/**
 * @author Liu Huanting 2015年5月10日
 * 语法树解析器
 */
public class RouteVisitor extends Visitor {
    private final Database       database;
    private ArrayList<Object>    values;
    private Table                table;
    private int                  info;
    private Set<String>          groupBy;
    private Map<String, Integer> orderBy;
    private int                  limitSize   = -1;
    private int                  limitOffset = 0;
    private int                  batchIndex  = -1;

    public RouteVisitor(Database database) {
        this.database = database;
        this.values = new ArrayList<Object>();
    }

    public Table getTable() {
        return table;
    }

    public ArrayList<Object> getValues() {
        return values;
    }

    public int getInfo() {
        return info;
    }

    public Set<String> getGroupBy() {
        return groupBy;
    }

    public Map<String, Integer> getOrderBy() {
        return orderBy;
    }

    public int getLimitSize() {
        return limitSize;
    }

    public int getLimitOffset() {
        return limitOffset;
    }

    public int getBatchIndex() {
        return batchIndex;
    }

    private void recordTable(Identifier node) {
        String table = node.getIdTextUpUnescape();
        this.table = database.getTables().get(table);
    }

    /**
     * 记录路由字段的值
     */
    private void recordValue(Identifier column, Object value) {
        if (value != null && value != Expression.UNEVALUATABLE) {
            if (check(column.getIdTextUpUnescape())) {
                values.add(value);
            }
        }
    }

    private boolean check(String column) {
        if (table != null && TableType.SPLIT.equals(table.getType())) {
            return table.getRule().getColumn().equals(column);
        }
        return false;
    }

    @Override
    public void visit(TableRefFactor node) {
        recordTable(node.getTable());

    }

    @Override
    public void visit(DDLCreateTableStatement node) {
        recordTable(node.getTable());
    }

    @Override
    public void visit(DDLDropTableStatement node) {
        for (Identifier table : node.getTableNames()) {
            recordTable(table);
        }
    }

    @Override
    public void visit(DMLInsertStatement node) {
        insertReplace(node);
    }

    @Override
    public void visit(DMLReplaceStatement node) {
        insertReplace(node);
    }

    public void insertReplace(DMLInsertReplaceStatement node) {
        Identifier table = node.getTable();
        List<Identifier> columns = node.getColumnNameList();
        List<RowExpression> rows = node.getRowList();
        recordTable(table);
        visitChild(columns);
        int i = 0;
        for (Identifier column : columns) {
            if (check(column.getIdTextUpUnescape())) {
                batchIndex = i;
                break;
            }
            i++;
        }
        for (RowExpression row : rows) {
            row.accept(this);
            if (batchIndex != -1) {
                recordValue(columns.get(batchIndex),
                        row.getRowExprList().get(batchIndex).evaluation(Collections.emptyMap()));
            }
        }
        visitChild(node.getSelect());
    }

    @Override
    public void visit(ComparisionEqualsExpression node) {
        Expression left = node.getLeftOprand();
        Expression right = node.getRightOprand();
        visitChild(left);
        visitChild(right);
        if (left instanceof Identifier) {
            recordValue((Identifier) left, right.evaluation(Collections.emptyMap()));
        } else if (right instanceof Identifier) {
            recordValue((Identifier) right, left.evaluation(Collections.emptyMap()));
        }
    }

    @Override
    public void visit(LogicalOrExpression node) {
        info |= Info.TO_ALL_NODE;
        visit((PolyadicOperatorExpression) node);
    }

    @Override
    public void visit(Max node) {
        info |= Info.NEED_MERGE;
    }

    @Override
    public void visit(Min node) {
        info |= Info.NEED_MERGE;
    }

    @Override
    public void visit(Sum node) {
        info |= Info.NEED_MERGE;
    }

    @Override
    public void visit(Count node) {
        info |= Info.NEED_MERGE;
    }

    @Override
    public void visit(GroupBy node) {
        List<Pair<Expression, SortOrder>> list = node.getOrderByList();
        if (list == null || list.isEmpty()) {
            return;
        }
        info |= Info.HAS_GROUPBY;
        this.groupBy = new HashSet<String>();
        for (Pair<Expression, SortOrder> pair : list) {
            String column = ((Identifier) pair.getKey()).getIdTextUpUnescape();
            switch (pair.getValue()) {
                case ASC:
                    groupBy.add(column);
                case DESC:
                    break;
            }
        }
    }

    @Override
    public void visit(OrderBy node) {
        List<Pair<Expression, SortOrder>> list = node.getOrderByList();
        if (list == null || list.isEmpty()) {
            return;
        }
        info |= Info.HAS_ORDERBY;
        this.orderBy = new HashMap<String, Integer>();
        for (Pair<Expression, SortOrder> pair : list) {
            String column = ((Identifier) pair.getKey()).getIdTextUpUnescape();
            switch (pair.getValue()) {
                case ASC:
                    orderBy.put(column, MergeType.ASC);
                    break;
                case DESC:
                    orderBy.put(column, MergeType.DESC);
                    break;
            }
        }
    }

    @Override
    public void visit(Limit limit) {
        info |= Info.HAS_LIMIT;
        limitSize = (int) limit.getSize();
        limitOffset = (int) limit.getOffset();
    }

}
