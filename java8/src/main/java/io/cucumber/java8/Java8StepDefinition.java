package io.cucumber.java8;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runtime.Invoker;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        this.expression = expression;
        this.parameterInfos = fromTypes(expression, location, resolveRawArguments(bodyClass, body.getClass()));
    }

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
        List<ParameterInfo> result = new ArrayList<>();
        for (Type type : genericParameterTypes) {
            LambdaTypeResolver typeResolver = new LambdaTypeResolver(type, expression, location);
            result.add(new Java8ParameterInfo(type, typeResolver));
        }
        return result;
    }

}
