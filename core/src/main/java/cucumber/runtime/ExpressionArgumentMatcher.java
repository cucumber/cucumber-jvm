package cucumber.runtime;

import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.Expression;

import java.util.List;

public class ExpressionArgumentMatcher implements ArgumentMatcher {
    private final Expression expression;

    public ExpressionArgumentMatcher(Expression expression) {
        this.expression = expression;
    }

    @Override
    public List<Argument<?>> argumentsFrom(String stepName) {
        return expression.match(stepName);
    }
}
