package fm.liu.timo.parser.ast.stmt.extension;

import java.util.ArrayList;
import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.stmt.SQLStatement;
import fm.liu.timo.parser.visitor.Visitor;

/**
 * @author liuhuanting
 */
public class ExecutePrepareStatement implements SQLStatement {

    private final String                name;
    private final ArrayList<Expression> vars;

    public ExecutePrepareStatement(String name, ArrayList<Expression> vars) {
        this.name = name;
        this.vars = vars;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Expression> getVars() {
        return vars;
    }

    @Override
    public void accept(Visitor visitor) {}

}
