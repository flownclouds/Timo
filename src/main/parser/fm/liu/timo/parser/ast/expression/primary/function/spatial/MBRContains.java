package fm.liu.timo.parser.ast.expression.primary.function.spatial;

import java.util.List;

import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.function.FunctionExpression;
import fm.liu.timo.parser.visitor.Visitor;

public class MBRContains extends FunctionExpression {

    public MBRContains(List<Expression> arguments) {
        super("MBRCONTAINS", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new MBRContains(arguments);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
