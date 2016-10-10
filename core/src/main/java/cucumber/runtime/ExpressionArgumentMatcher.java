package cucumber.runtime;

import io.cucumber.cucumberexpressions.Expression;

import java.util.ArrayList;
import java.util.List;

public class ExpressionArgumentMatcher implements ArgumentMatcher {
    private final Expression expression;

    public ExpressionArgumentMatcher(Expression expression) {
        this.expression = expression;
    }

    @Override
    public List<Argument> argumentsFrom(String stepName) {
        List<io.cucumber.cucumberexpressions.Argument> args = expression.match(stepName);
        if(args == null) return null;
        List<Argument> result = new ArrayList<Argument>(args.size());
        for (io.cucumber.cucumberexpressions.Argument arg : args) {
            result.add(new Argument(arg.getOffset(), arg.getValue()));
        }
        return result;
    }
}
