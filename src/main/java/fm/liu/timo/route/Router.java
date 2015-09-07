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
package fm.liu.timo.route;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.config.model.Function;
import fm.liu.timo.config.model.Table;
import fm.liu.timo.config.model.Table.TableType;
import fm.liu.timo.parser.ast.expression.primary.RowExpression;
import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLInsertReplaceStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;
import fm.liu.timo.parser.visitor.OutputVisitor;
import fm.liu.timo.route.visitor.RouteVisitor;
import fm.liu.timo.server.parser.ServerParse;

/**
 * 路由计算器
 * @author Liu Huanting 2015年5月10日
 */
public class Router {
    // hint格式示例： 
    // /*!timo:node1,3*/select * from table_a; 仅在节点1和节点3上执行该语句
    // /*!timo:master*/select * from table_a; 读写分离时强制在主库上执行该语句
    private static final String HINT   = "/*!timo:";
    private static final String NODE   = "node";
    private static final String MASTER = "master";

    public static Outlets route(Database database, String sql, String charset, int type)
            throws SQLSyntaxErrorException {
        Outlets outlets = new Outlets();
        sql = sql.trim();
        if (sql.startsWith(HINT)) {
            int end = sql.indexOf("*/");
            if (end > 0) {
                String hint = sql.substring(HINT.length(), end).trim();
                sql = sql.substring(end + "*/".length());
                type = ServerParse.parse(sql) & 0xff;
                if (hint.startsWith(NODE)) {
                    String[] nodes = hint.substring(NODE.length()).split(",");
                    for (String node : nodes) {
                        int id = Integer.parseInt(node);
                        if (!database.getNodes().contains(id)) {
                            throw new IllegalArgumentException(
                                    "unknown datanoe" + id + " in hint:" + hint);
                        }
                        outlets.add(new Outlet(id, sql));
                    }
                    return outlets;
                } else if (hint.startsWith(MASTER)) {
                    outlets.setUsingMaster(true);
                } else {
                    Logger.warn("unsupported hint: {}", sql);
                }
            }
        }
        SQLStatement stmt = SQLParserDelegate.parse(sql, charset);
        RouteVisitor visitor = new RouteVisitor(database);
        stmt.accept(visitor);
        Table table = visitor.getTable();
        if (table == null) {
            outlets.add(new Outlet(database.getRandomNode(), sql));
            return outlets;
        }
        ArrayList<Object> values = visitor.getValues();
        int info = visitor.getInfo();
        outlets.setInfo(info);
        switch (type) {
            case ServerParse.SELECT:
                if (TableType.GLOBAL.equals(table.getType())) {
                    outlets.add(new Outlet(table.getRandomNode(), sql));
                    return outlets;
                }
                if ((info & Info.HAS_GROUPBY) == Info.HAS_GROUPBY) {
                    outlets.setGroupBy(visitor.getGroupBy());
                }
                if ((info & Info.HAS_ORDERBY) == Info.HAS_ORDERBY) {
                    outlets.setOrderBy(visitor.getOrderBy());
                }
                if ((info & Info.HAS_LIMIT) == Info.HAS_LIMIT) {
                    outlets.setLimit(visitor.getLimitSize(), visitor.getLimitOffset());
                }
                break;
            case ServerParse.INSERT:
            case ServerParse.REPLACE:
                if (TableType.SPLIT.equals(table.getType())) {
                    return routeBatch(table, (DMLInsertReplaceStatement) stmt,
                            visitor.getBatchIndex(), type, outlets, values);
                }
                break;
        }
        if ((info & Info.TO_ALL_NODE) == Info.TO_ALL_NODE) {
            return toAllNode(outlets, table, sql);
        }
        return route(stmt, outlets, table, values, sql);
    }

    private static Outlets toAllNode(Outlets outlets, Table table, String sql) {
        table.getNodes().forEach(i -> outlets.add(new Outlet(i, sql)));
        return outlets;
    }

    /**
     * <pre>
     * turn
     *     INSERT/REPLACE INTO TABLE_A(COL_1,COL2,...) VALUES (VAL_11,VAL12,...),(VAL_21,VAL22,...),(VAL_31,VAL32,...)...;
     * into something like
     *     INSERT/REPLACE INTO TABLE_A(COL_1,COL2,...) VALUES (VAL_11,VAL12,...),(VAL_31,VAL_32,...)...;
     *     INSERT/REPLACE INTO TABLE_A(COL_1,COL2,...) VALUES (VAL_21,VAL22,...),(VAL_41,VAL_42,...)...;
     * </pre>
     */
    private static Outlets routeBatch(Table table, DMLInsertReplaceStatement stmt, int index,
            int type, Outlets outlets, ArrayList<Object> values) {
        List<RowExpression> rows = stmt.getRowList();
        HashMap<Integer, List<RowExpression>> results = new HashMap<>();
        if (values.isEmpty()) {
            throw new IllegalArgumentException("can't route without the value of split column");
        } else {
            Function function = table.getRule().getFunction();
            int i = 0;
            for (Object value : values) {
                int node = function.calcute(value);
                if (results.containsKey(node)) {
                    results.get(node).add(rows.get(i++));
                } else {
                    List<RowExpression> exps = new ArrayList<>();
                    exps.add(rows.get(i++));
                    results.put(node, exps);
                }
            }
            for (Entry<Integer, List<RowExpression>> entry : results.entrySet()) {
                stmt.setReplaceRowList(entry.getValue());
                outlets.add(new Outlet(entry.getKey(), updateSQL(stmt)));
                stmt.clearReplaceRowList();
            }
        }
        return outlets;
    }

    private static String updateSQL(SQLStatement stmt) {
        OutputVisitor visitor = new OutputVisitor(new StringBuilder(), true);
        stmt.accept(visitor);
        return visitor.getSql();
    }

    private static Outlets route(SQLStatement stmt, Outlets outlets, Table table,
            ArrayList<Object> values, String sql) {
        if (!(stmt instanceof DDLStatement)) {
            sql = updateSQL(stmt);
        }
        if (values.isEmpty()) {
            for (Integer id : table.getNodes()) {
                Outlet out = new Outlet(id, sql);
                outlets.add(out);
            }
        } else {
            Function function = table.getRule().getFunction();
            Set<Integer> result = function.calcute(values);
            for (int id : result) {
                Outlet out = new Outlet(id, sql);
                outlets.add(out);
            }
        }
        return outlets;
    }
}
