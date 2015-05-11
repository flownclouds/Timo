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
package re.ovo.timo.route.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import re.ovo.timo.config.model.Database;
import re.ovo.timo.config.model.Table;
import re.ovo.timo.config.model.Table.TableType;
import re.ovo.timo.parser.ast.expression.Expression;
import re.ovo.timo.parser.ast.expression.comparison.ComparisionEqualsExpression;
import re.ovo.timo.parser.ast.expression.primary.Identifier;
import re.ovo.timo.parser.ast.fragment.tableref.TableRefFactor;
import re.ovo.timo.parser.ast.stmt.ddl.DDLCreateTableStatement;
import re.ovo.timo.parser.visitor.Visitor;

/**
 * @author Liu Huanting
 * 2015年5月10日
 */
public class RouteVisitor extends Visitor {
    private final Database database;
    private Set<Object> values;
    private Table table;
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


    private boolean check(String column) {
        if (table != null && TableType.SPLIT.equals(table.getType())) {
            return table.getRule().getColumn().equals(column);
        }
        return false;
    }


}
