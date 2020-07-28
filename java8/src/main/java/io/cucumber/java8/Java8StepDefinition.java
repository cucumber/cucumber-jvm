package io.cucumber.java8;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class Java8StepDefinition extends AbstractGlueDefinition implements StepDefinition {

    private final List<ParameterInfo> parameterInfos;
    private final String expression;

    private <T extends StepDefinitionBody> Java8StepDefinition(
            String expression,
            Class<T> bodyClass,
            T body
    ) {
        super(body, new Exception().getStackTrace()[3]);
        this.expression = requireNonNull(expression, "cucumber-expression may not be null");
        this.parameterInfos = fromTypes(expression, location, resolveRawArguments(bodyClass, body.getClass()));
    }

    private static List<ParameterInfo> fromTypes(
            String expression, StackTraceElement location, Type[] genericParameterTypes
    ) {
        return Arrays.stream(genericParameterTypes)
                .map(type -> new LambdaTypeResolver(type, expression, location))
                .map(Java8ParameterInfo::new)
                .collect(toList());
    }

    public static <T extends StepDefinitionBody> Java8StepDefinition create(
            String expression, Class<T> bodyClass, T body
    ) {
        return new Java8StepDefinition(expression, bodyClass, body);
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
