package fm.liu.timo.parser.ast.expression.primary.function.spatial;

import java.util.List;

import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.function.FunctionExpression;
import fm.liu.timo.parser.visitor.Visitor;

public class MultiLineString extends FunctionExpression {

    public MultiLineString(List<Expression> arguments) {
        super("MULTILINESTRING", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new MultiLineString(arguments);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
