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
import fm.liu.timo.parser.ast.expression.comparison.ComparisionEqualsExpression;
import fm.liu.timo.parser.ast.expression.primary.Identifier;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Count;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Max;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Min;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Sum;
import fm.liu.timo.parser.ast.fragment.GroupBy;
import fm.liu.timo.parser.ast.fragment.OrderBy;
import fm.liu.timo.parser.ast.fragment.SortOrder;
import fm.liu.timo.parser.ast.fragment.tableref.TableRefFactor;
import fm.liu.timo.parser.ast.stmt.ddl.DDLCreateTableStatement;
import fm.liu.timo.parser.util.Pair;
import fm.liu.timo.parser.visitor.Visitor;
import fm.liu.timo.route.Info;

/**
 * @author Liu Huanting 2015年5月10日
 */
public class RouteVisitor extends Visitor {
    private final Database database;
    private Set<Object> values;
    private Table table;
    private int info;
    private Set<String> groupBy;
    private Map<String, Integer> orderBy;
    private final Map<Object, Object> evaluationParameter = Collections.emptyMap();

    public RouteVisitor(Database database) {
        this.database = database;
        this.values = new HashSet<Object>();
    }

    public Table getTable() {
        return table;
    }

    public Set<Object> getValues() {
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

    private void recordTable(Identifier node) {
        String table = node.getIdTextUpUnescape();
        this.table = database.getTables().get(table);
    }

    private void recordValue(Identifier column, Object value, Expression node) {
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
    public void visit(ComparisionEqualsExpression node) {
        Expression left = node.getLeftOprand();
        Expression right = node.getRightOprand();
        visitChild(left);
        visitChild(right);
        if (left instanceof Identifier) {
            recordValue((Identifier) left, right.evaluation(evaluationParameter), node);
        } else if (right instanceof Identifier) {
            recordValue((Identifier) right, left.evaluation(evaluationParameter), node);
        }
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

}
