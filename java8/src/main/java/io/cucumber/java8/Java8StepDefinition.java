package io.cucumber.java8;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runtime.Invoker;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

final class Java8StepDefinition extends AbstractGlueDefinition implements StepDefinition {

    public static <T extends StepdefBody> Java8StepDefinition create(
        String expression, Class<T> bodyClass, T body) {
        return new Java8StepDefinition(expression, 0, bodyClass, body);
    }

    public static <T extends StepdefBody> StepDefinition create(
        String expression, long timeoutMillis, Class<T> bodyClass, T body) {
        return new Java8StepDefinition(expression, timeoutMillis, bodyClass, body);
    }

    private final long timeoutMillis;
    private final List<ParameterInfo> parameterInfos;
    private final String expression;

    private <T extends StepdefBody> Java8StepDefinition(String expression,
                                                        long timeoutMillis,
                                                        Class<T> bodyClass,
                                                        T body) {
        super(body, new Exception().getStackTrace()[3]);
        this.timeoutMillis = timeoutMillis;
        this.expression = requireNonNull(expression, "cucumber-expression may not be null");
        this.parameterInfos = fromTypes(expression, location, resolveRawArguments(bodyClass, body.getClass()));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(final Object[] args) throws Throwable {
        Invoker.invoke(body, method, timeoutMillis, args);
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return parameterInfos;
    }

    @Override
    public String getPattern() {
        return expression;
    }

    private static List<ParameterInfo> fromTypes(String expression, StackTraceElement location, Type[] genericParameterTypes) {
        return Arrays.stream(genericParameterTypes)
            .map(type -> new LambdaTypeResolver(type, expression, location))
            .map(Java8ParameterInfo::new)
            .collect(toList());
    }

}
