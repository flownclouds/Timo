package fm.liu.timo.parser.ast.expression.primary.function.datetime;

import java.util.List;

import fm.liu.timo.parser.ast.expression.Expression;
import fm.liu.timo.parser.ast.expression.primary.function.FunctionExpression;

public class CurTimestamp extends FunctionExpression {

    public CurTimestamp() {
        super("CURTIMTSTAMP", null);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new CurTimestamp();
    }

}
