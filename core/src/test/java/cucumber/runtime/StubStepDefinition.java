package cucumber.runtime;

import io.cucumber.stepexpression.TypeRegistry;
import io.cucumber.stepexpression.Argument;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.ArgumentMatcher;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StubStepDefinition implements StepDefinition {
    private final StepExpression expression;
    private List<ParameterInfo> parameterInfos;

    StubStepDefinition(String pattern, TypeRegistry typeRegistry, Type... types) {
        this.parameterInfos = ParameterInfo.fromTypes(types);
        if (parameterInfos.isEmpty()) {
            this.expression = new StepExpressionFactory(typeRegistry).createExpression(pattern);
        } else {
            ParameterInfo lastParameter = parameterInfos.get(parameterInfos.size() - 1);
            this.expression = new StepExpressionFactory(typeRegistry).createExpression(pattern, lastParameter.getType());
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
        return parameterInfos.size();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return parameterInfos.get(n);
    }

    @Override
    public void execute(String language, Object[] args) {
        assertEquals(parameterInfos.size(), args.length);
        for (int i = 0; i < args.length; i++) {
            assertEquals(parameterInfos.get(i).getType(), args[i].getClass());
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
