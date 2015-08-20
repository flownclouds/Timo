package fm.liu.timo.parser;

import java.sql.SQLSyntaxErrorException;
import junit.framework.Assert;
import org.junit.Test;
import fm.liu.timo.parser.ast.expression.primary.Identifier;
import fm.liu.timo.parser.ast.expression.primary.RowExpression;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralNumber;
import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLInsertStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;
import fm.liu.timo.parser.visitor.OutputVisitor;

public class TestInsertSQLAnalyser {

    @Test
    public void testInsertSQL() throws SQLSyntaxErrorException {
        String sql = null;
        SQLStatement ast = null;
        DMLInsertStatement parsInf = null;
        sql = "insert into table1  select * FROM table2 WHERE id not in ( select id from  table1) ";
        // sql =
        // "insert into table1  select * FROM table2 WHERE id not in ( select id aT\'b  table1) ";
        ast = SQLParserDelegate.parse(sql);
        parsInf = (DMLInsertStatement) (ast);
        Assert.assertEquals("table1".toUpperCase(), parsInf.getTable().getIdTextUpUnescape());
        Assert.assertEquals(null, parsInf.getColumnNameList());

        sql = "insert into table1(column1,column2,column3,colum4,column5,column6,column7)values('aaa',5,'1999-2-2',true,\"test\",111,55.66) ";
        ast = SQLParserDelegate.parse(sql);
        parsInf = (DMLInsertStatement) (ast);
        Assert.assertEquals("table1".toUpperCase(), parsInf.getTable().getIdTextUpUnescape());
        Assert.assertEquals(7, parsInf.getColumnNameList().size());

        sql = "inSErt into table1 (`offer_id`, gmt) values (123,now())";
        ast = SQLParserDelegate.parse(sql);
        parsInf = (DMLInsertStatement) (ast);
        Assert.assertEquals("table1".toUpperCase(), parsInf.getTable().getIdTextUpUnescape());
        Assert.assertEquals(2, parsInf.getColumnNameList().size());

        sql = "insert into table1 (offer_id, gmt) values (0, now()), (1, now()), (2, now())";
        ast = SQLParserDelegate.parse(sql);
        parsInf = (DMLInsertStatement) (ast);
        Assert.assertEquals("table1".toUpperCase(), parsInf.getTable().getIdTextUpUnescape());
        Assert.assertEquals(2, parsInf.getColumnNameList().size());

        sql = "insert  into table1(USER_ID,USER_NAME,PASSWORD,CREATE_TIME,STATUS,NICK_NAME,USER_ICON_URL,USER_ICON_URL2,USER_ICON_URL3,ACCOUNT_TYPE) "
                + "values (2488899998,'u163149830250134','af8f9dffa5d420fbc249141645b962ee','2013-12-01 00:00:00',0,NULL,NULL,NULL,NULL,1)";
        ast = SQLParserDelegate.parse(sql);
        parsInf = (DMLInsertStatement) (ast);
        Assert.assertEquals("table1".toUpperCase(), parsInf.getTable().getIdTextUpUnescape());
        Assert.assertEquals(10, parsInf.getColumnNameList().size());
    }

    @Test
    public void testInsertSQLWithAutoIncrement() throws SQLSyntaxErrorException {
        Identifier columnAutoIncrementColumn = new Identifier(null, "id");
        String sql = null;
        SQLStatement ast = null;
        DMLInsertStatement parsInf = null;
        sql = "insert into table1(name1,name2,id)  values ('1','2',10) ";
        ast = SQLParserDelegate.parse(sql);
        parsInf = (DMLInsertStatement) (ast);
        enrichAutoIncrementColumn(columnAutoIncrementColumn, parsInf);

        StringBuilder sb = new StringBuilder();
        OutputVisitor visitor = new OutputVisitor(sb, false);
        parsInf.accept(visitor);
        System.out.println(sb.toString());
        Assert.assertEquals("table1".toUpperCase(), parsInf.getTable().getIdTextUpUnescape());
        Assert.assertNotNull(parsInf.getColumnNameList());
    }

    /**
     * 判断insert语句中是否包含自增序列字段，如果没有包含，则进行补充
     * 
     * @param columnAutoIncrementColumn
     * @param ddl
     */
    private void enrichAutoIncrementColumn(Identifier columnAutoIncrementColumn,
            DMLInsertStatement ddl) {
        boolean isEnrichAutoIncrementColumn = true;
        for (Identifier column : ddl.getColumnNameList()) {
            if (column.getIdTextUpUnescape()
                    .equals(columnAutoIncrementColumn.getIdTextUpUnescape())) {
                isEnrichAutoIncrementColumn = false;
            }
        }
        if (isEnrichAutoIncrementColumn) {
            ddl.getColumnNameList().add(columnAutoIncrementColumn);
            for (RowExpression row : ddl.getRowList()) {
                row.getRowExprList().add(new LiteralNumber(new Integer(1)));
            }
        }
    }
}
