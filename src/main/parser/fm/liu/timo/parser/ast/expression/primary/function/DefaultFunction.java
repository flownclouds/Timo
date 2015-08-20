package fm.liu.timo.parser.ast.expression.primary.function;

import java.util.List;
import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.visitor.Visitor;

public class DefaultFunction extends FunctionExpression {
    private String functionName;

    public DefaultFunction(String functionName, List<Expression> arguments) {
        super(functionName, arguments);
        this.functionName = functionName;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new DefaultFunction(functionName, arguments);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
