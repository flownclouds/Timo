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
import java.util.Set;

import fm.liu.timo.config.model.Database;
import fm.liu.timo.config.model.Function;
import fm.liu.timo.config.model.Table;
import fm.liu.timo.config.model.Table.TableType;
import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;
import fm.liu.timo.parser.visitor.OutputVisitor;
import fm.liu.timo.route.visitor.RouteVisitor;
import fm.liu.timo.server.parser.ServerParse;

/**
 * @author Liu Huanting 2015年5月10日
 */
public class Router {
    public static Outlets route(Database database, String sql, String charset, int type)
            throws SQLSyntaxErrorException {
        Outlets outlets = new Outlets();
        SQLStatement stmt = SQLParserDelegate.parse(sql, charset);
        RouteVisitor visitor = new RouteVisitor(database);
        stmt.accept(visitor);
        Table table = visitor.getTable();
        if (table == null) {
            Outlet out = new Outlet(database.getRandomNode(), sql);
            outlets.add(out);
            return outlets;
        }
        Set<Object> values = visitor.getValues();
        int info = visitor.getInfo();
        outlets.setInfo(info);
        switch (type) {
            case ServerParse.SELECT:
                if (TableType.GLOBAL.equals(table.getType())) {
                    Outlet out = new Outlet(table.getRandomNode(), sql);
                    outlets.add(out);
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
        }
        return route(stmt, outlets, table, values, sql);
    }

    private static String updateSQL(SQLStatement stmt) {
        OutputVisitor visitor = new OutputVisitor(new StringBuilder(), true);
        stmt.accept(visitor);
        return visitor.getSql();
    }

    private static Outlets route(SQLStatement stmt, Outlets outlets, Table table,
            Set<Object> values, String sql) {
        if (values.isEmpty()) {
            for (Integer id : table.getNodes()) {
                Outlet out = new Outlet(id, sql);
                outlets.add(out);
            }
        } else {
            Function function = table.getRule().getFunction();
            Set<Integer> result = function.calcute(values);
            if (result.size() > 1) {
                sql = updateSQL(stmt);
            }
            for (int id : result) {
                Outlet out = new Outlet(id, sql);
                outlets.add(out);
            }
        }
        return outlets;
    }
}
