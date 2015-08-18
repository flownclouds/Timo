/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
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
package fm.liu.timo.parser;

import java.sql.SQLSyntaxErrorException;

import org.junit.Test;

import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLSelectStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;
import fm.liu.timo.parser.recognizer.mysql.lexer.MySQLLexer;
import fm.liu.timo.parser.recognizer.mysql.syntax.MySQLDMLSelectParser;
import fm.liu.timo.parser.recognizer.mysql.syntax.MySQLExprParser;
import fm.liu.timo.parser.visitor.OutputVisitor;

public class CobarParserTest {

    @Test
    public void parseWithoutDelegateClass() throws Exception {
        System.out.println();
        String sql =
                "select t1.name,t2.productid,t3.name from customer t1,orderlist t2,product t3 where t1.id=t2.customerid and t2.productid=t3.id;";
        MySQLLexer lexer = new MySQLLexer(sql);
        MySQLDMLSelectParser parser = new MySQLDMLSelectParser(lexer, new MySQLExprParser(lexer));
        DMLSelectStatement select = parser.select();
        StringBuilder sb = new StringBuilder();
        select.accept(new OutputVisitor(sb, false));
        System.out.println(sb.toString());
    }

    // @Test
    // public void parseLoadDataFile() throws Exception {
    // String sql =
    // "LOAD DATA local INFILE ' /Users/lcz/data1.txt' INTO TABLE company1   FIELDS TERMINATED BY ',';SELECT     UUID();--aaaaa";
    // String outSql = parseAndFormatSql(sql);
    // System.out.println(outSql);
    // }

    // @Test
    // public void parseSelectIntoOutFile() throws Exception {
    // String sql =
    // "SELECT * INTO OUTFILE ' data1.txt' FIELDS TERMINATED BY ',' FROM company  limit 2;";
    // String outSql = parseAndFormatSql(sql);
    // System.out.println(outSql);
    // }

    @Test
    public void parseSelectFromMultiTable() throws Exception {
        String sql =
                "select t1.name,t2.productid,t3.name from customer t1,orderlist t2,product t3 where t1.id=t2.customerid and t2.productid=t3.id;";
        String outSql = parseAndFormatSql(sql);
        System.out.println(outSql);
    }

    public static void main(String[] args) throws Exception {
        new CobarParserTest().parseSelectFromMultiTable();
    }

    private String parseAndFormatSql(String sql) throws SQLSyntaxErrorException {
        SQLStatement ast = SQLParserDelegate.parse(sql);
        StringBuilder sb = new StringBuilder();
        ast.accept(new OutputVisitor(sb, false));
        return sb.toString();
    }

    static final int Count = 1000;

    @Test
    public void perfSql() throws SQLSyntaxErrorException {
        String formatSql = null;
        String sql =
                "select t1.name,t2.productid,t3.name from customer t1,orderlist t2,product t3 where t1.id=t2.customerid and t2.productid=t3.id;";

        long startMillis = System.currentTimeMillis();
        for (int i = 0; i < Count; ++i) {
            formatSql = parseAndFormatSql(sql);
        }
        long millis = System.currentTimeMillis() - startMillis;
        System.out.println(formatSql);
        System.out.println("Cobar parse use " + millis + "ms,every use " + millis * 1000 / Count);
    }
}
