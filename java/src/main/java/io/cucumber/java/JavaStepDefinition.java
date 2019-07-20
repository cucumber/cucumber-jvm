package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.reflection.MethodFormat;
import io.cucumber.core.runtime.Invoker;

import java.lang.reflect.Method;
import java.util.List;

final class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final String expression;
    private final long timeoutMillis;
    private final Lookup lookup;

    private final String shortFormat;
    private final String fullFormat;
    private final List<ParameterInfo> parameterInfos;

    JavaStepDefinition(Method method,
                       String expression,
                       long timeoutMillis,
                       Lookup lookup) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.lookup = lookup;
        this.parameterInfos = JavaParameterInfo.fromMethod(method);
        this.expression = expression;
        this.shortFormat = MethodFormat.SHORT.format(method);
        this.fullFormat = MethodFormat.FULL.format(method);
    }

    @Override
    public void execute(Object[] args) throws Throwable {
        Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    @Override
    public String getLocation(boolean detail) {
        return detail ? fullFormat : shortFormat;
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
