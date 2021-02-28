package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class JavaStepDefinition extends AbstractGlueDefinition implements StepDefinition {

    private final String expression;
    private final List<ParameterInfo> parameterInfos;

    JavaStepDefinition(
            Method method,
            String expression,
            Lookup lookup
    ) {
        super(method, lookup);
        this.parameterInfos = JavaParameterInfo.fromMethod(method);
        this.expression = requireNonNull(expression, "cucumber-expression may not be null");
    }

    @Override
    public void execute(Object[] args) {
        invokeMethod(args);
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return parameterInfos;
    }

    @Override
    public String getPattern() {
        return expression;
    }

}
