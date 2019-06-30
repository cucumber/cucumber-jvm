package io.cucumber.core.runtime;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.stepexpression.Argument;
import gherkin.pickles.PickleStep;
import io.cucumber.core.stepexpression.ArgumentMatcher;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StubStepDefinition implements StepDefinition {
    private final List<Type> parameters;
    private final StepExpression expression;
    private final ArgumentMatcher argumentMatcher;

    public StubStepDefinition(String pattern, TypeRegistry typeRegistry, Type... types) {
        this.parameters = Arrays.asList(types);
        if (parameters.isEmpty()) {
            this.expression = new StepExpressionFactory(typeRegistry).createExpression(pattern);
        } else {
            Type lastParameter = parameters.get(parameters.size() - 1);
            this.expression = new StepExpressionFactory(typeRegistry).createExpression(pattern, lastParameter);
        }
        this.argumentMatcher = new ArgumentMatcher(expression);
    }

    @Override
    public List<Argument> matchedArguments(PickleStep step) {
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

}
