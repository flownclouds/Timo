package fm.liu.timo.parser;

import java.sql.SQLSyntaxErrorException;

import junit.framework.Assert;

import org.junit.Test;

import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLCallStatement;
import fm.liu.timo.parser.recognizer.SQLParserDelegate;

public class TestCallQLAnalyser {

    @Test
    public void testCallSQL() throws SQLSyntaxErrorException {
        String sql = null;
        SQLStatement ast = null;

        sql = "call proc_rtn_list(10000)";
        ast = SQLParserDelegate.parse(sql);

        Assert.assertTrue(ast instanceof DMLCallStatement);
    }
}
