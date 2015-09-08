package fm.liu.timo.parser.ast.stmt.extension;

import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.visitor.Visitor;

/**
 * @author liuhuanting
 */
public class PrepareStatement implements SQLStatement {
    private final String name;
    private final String stmt;

    public PrepareStatement(String name, String stmt) {
        this.name = name;
        this.stmt = stmt;
    }

    public String getName() {
        return name;
    }

    public String getStatement() {
        return stmt;
    }

    @Override
    public void accept(Visitor visitor) {}
}
