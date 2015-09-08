package fm.liu.timo.parser.recognizer.mysql.syntax;

import java.sql.SQLSyntaxErrorException;
import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.Identifier;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralString;
import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.ast.stmt.extension.PrepareStatement;
import fm.liu.timo.parser.recognizer.mysql.MySQLToken;
import fm.liu.timo.parser.recognizer.mysql.lexer.MySQLLexer;

/**
 * @author liuhuanting
 */
public class MySQLPrepareParser extends MySQLParser {

    protected MySQLExprParser exprParser;

    public MySQLPrepareParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer);
        this.exprParser = exprParser;
    }

    public SQLStatement prepare() throws SQLSyntaxErrorException {
        lexer.nextToken();
        Expression stmt_name = exprParser.expression();
        if (!(stmt_name instanceof Identifier)) {
            throw new SQLSyntaxErrorException("SQL syntax error!");
        }
        match(MySQLToken.KW_FROM);
        Expression preparable_stmt = exprParser.expression();
        if (!(preparable_stmt instanceof LiteralString)) {
            throw new SQLSyntaxErrorException("SQL syntax error!");
        }
        return new PrepareStatement(((Identifier) stmt_name).getIdTextUpUnescape(),
                ((LiteralString) preparable_stmt).getString());
    }

    public SQLStatement execute() {
        return null;
    }

}
