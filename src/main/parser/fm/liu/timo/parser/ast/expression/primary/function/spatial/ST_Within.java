package fm.liu.timo.parser.ast.expression.primary.function.spatial;

import java.util.List;

import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.function.FunctionExpression;
import fm.liu.timo.parser.visitor.Visitor;

public class ST_Within extends FunctionExpression {

    public ST_Within(List<Expression> arguments) {
        super("ST_WITHIN", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new ST_Within(arguments);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
