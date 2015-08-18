package fm.liu.timo.parser.ast.expression.primary.function.spatial;

import java.util.List;

import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.function.FunctionExpression;
import fm.liu.timo.parser.visitor.Visitor;

public class Point extends FunctionExpression {

    public Point(List<Expression> arguments) {
        super("POINT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Point(arguments);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
