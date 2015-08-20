package fm.liu.timo.parser;

import java.sql.SQLSyntaxErrorException;
import junit.framework.Assert;
import org.junit.Test;
import fm.liu.timo.parser.ast.expression.primary.Identifier;
import fm.liu.timo.parser.ast.fragment.ddl.ColumnDefinition;
import fm.liu.timo.parser.ast.stmt.ddl.DDLCreateTableStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;
import fm.liu.timo.parser.util.Pair;

public class TestDDLSQLAnalyser {
    @Test
    public void testSQL() throws SQLSyntaxErrorException {
        String sql =
                "CREATE TABLE Persons ( Id_P int,LastName varchar(255),FirstName varchar(255),Address varchar(255),City varchar(255))";
        DDLCreateTableStatement ast = null;
        ast = (DDLCreateTableStatement) SQLParserDelegate.parse(sql);
        System.out.println(ast.getTable());
        Assert.assertEquals("Persons".toUpperCase(), ast.getTable().getIdTextUpUnescape());
    }

    @Test
    public void testSQLAutoIncrement() throws SQLSyntaxErrorException {
        String sql = "create table t6 (id int auto_increment,name varchar(20) primary key,key(id))";
        DDLCreateTableStatement ast = null;
        ast = (DDLCreateTableStatement) SQLParserDelegate.parse(sql);
        String columnAutoIncrement = null;
        for (Pair<Identifier, ColumnDefinition> pair : ast.getColDefs()) {
            if (pair.getValue().isAutoIncrement()) {
                columnAutoIncrement = pair.getKey().getIdTextUpUnescape();
            }
        }
        System.out.println("columnAutoIncrement:" + columnAutoIncrement);
        Assert.assertEquals("T6".toUpperCase(), ast.getTable().getIdTextUpUnescape());
    }
}
