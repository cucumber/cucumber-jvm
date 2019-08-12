package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runtime.Invoker;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class JavaStepDefinition extends AbstractGlueDefinition implements StepDefinition {
    private final String expression;
    private final long timeoutMillis;

    private final List<ParameterInfo> parameterInfos;

    JavaStepDefinition(Method method,
                       String expression,
                       long timeoutMillis,
                       Lookup lookup) {
        super(method, lookup);
        this.timeoutMillis = timeoutMillis;
        this.parameterInfos = JavaParameterInfo.fromMethod(method);
        this.expression = requireNonNull(expression, "cucumber-expression may not be null");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(Object[] args) throws Throwable {
        Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return expression;
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return parameterInfos;
    }

}
