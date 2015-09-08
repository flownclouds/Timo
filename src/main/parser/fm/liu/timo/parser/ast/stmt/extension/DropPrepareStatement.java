package fm.liu.timo.parser.ast.stmt.extension;

import fm.liu.timo.parser.ast.stmt.ddl.DDLStatement;
import fm.liu.timo.parser.visitor.Visitor;

/**
 * @author liuhuanting
 */
public class DropPrepareStatement implements DDLStatement {
    private final String name;

    public DropPrepareStatement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(Visitor visitor) {}

}
