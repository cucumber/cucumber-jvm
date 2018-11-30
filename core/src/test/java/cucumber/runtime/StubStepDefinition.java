package cucumber.runtime;

import io.cucumber.stepexpression.TypeRegistry;
import io.cucumber.stepexpression.Argument;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.ArgumentMatcher;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StubStepDefinition implements StepDefinition {
    private final StepExpression expression;
    private List<Type> parameters;

    public StubStepDefinition(String pattern, TypeRegistry typeRegistry, Type... types) {
        this.parameters = Arrays.asList(types);
        if (parameters.isEmpty()) {
            this.expression = new StepExpressionFactory(typeRegistry).createExpression(pattern);
        } else {
            Type lastParameter = parameters.get(parameters.size() - 1);
            this.expression = new StepExpressionFactory(typeRegistry).createExpression(pattern, lastParameter);
        }
    }

    @Override
    public List<Argument> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        return argumentMatcher.argumentsFrom(step);
    }

    @Override
    public String getLocation(boolean detail) {
        return "{stubbed location" + (detail ? " with details" : "") + "}";
    }

    @Override
    public Integer getParameterCount() {
        return parameters.size();
    }

    @Override
    public void execute(Object[] args) throws Throwable {
        assertEquals(parameters.size(), args.length);
        for (int i = 0; i < args.length; i++) {
            assertEquals(parameters.get(i), args[i].getClass());
        }
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getPattern() {
        return expression.getSource();
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
