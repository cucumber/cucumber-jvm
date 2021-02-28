package io.cucumber.java;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;

import java.lang.reflect.Method;

import static io.cucumber.java.InvalidMethodSignatureException.builder;
import static java.util.Objects.requireNonNull;

final class JavaHookDefinition extends AbstractGlueDefinition implements HookDefinition {

    private final String tagExpression;
    private final int order;

    JavaHookDefinition(Method method, String tagExpression, int order, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.tagExpression = requireNonNull(tagExpression, "tag-expression may not be null");
        this.order = order;
    }

    private static Method requireValidMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 1) {
            throw createInvalidSignatureException(method);
        }

        if (parameterTypes.length == 1) {
            Class<?> parameterType = parameterTypes[0];
            if (!(Object.class.equals(parameterType) || io.cucumber.java.Scenario.class.equals(parameterType))) {
                throw createInvalidSignatureException(method);
            }
        }

        return method;
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
                .addAnnotation(Before.class)
                .addAnnotation(After.class)
                .addAnnotation(BeforeStep.class)
                .addAnnotation(AfterStep.class)
                .addSignature("public void before_or_after(io.cucumber.java.Scenario scenario)")
                .addSignature("public void before_or_after()")
                .build();
    }

    @Override
    public void execute(TestCaseState state) {
        Object[] args;
        if (method.getParameterTypes().length == 1) {
            args = new Object[] { new io.cucumber.java.Scenario(state) };
        } else {
            args = new Object[0];
        }

        invokeMethod(args);
    }

    @Override
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
