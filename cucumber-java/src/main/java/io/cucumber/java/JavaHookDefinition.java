package io.cucumber.java;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.messages.types.HookType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.cucumber.java.InvalidMethodSignatureException.builder;
import static java.util.Objects.requireNonNull;

final class JavaHookDefinition extends AbstractGlueDefinition implements HookDefinition {

    private final String tagExpression;
    private final int order;
    private final HookType hookType;

    JavaHookDefinition(HookType hookType, Method method, String tagExpression, int order, Lookup lookup) {
        super(requireValidMethod(hookType, method), lookup);
        this.hookType = requireNonNull(hookType);
        this.tagExpression = requireNonNull(tagExpression, "tag-expression may not be null");
        this.order = order;
    }

    private static Method requireValidMethod(HookType hookType, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean isStepHook = hookType == HookType.BEFORE_STEP || hookType == HookType.AFTER_STEP;

        // Step hooks allow 0, 1, or 2 parameters; other hooks allow 0 or 1
        int maxParams = isStepHook ? 2 : 1;
        if (parameterTypes.length > maxParams) {
            throw createInvalidSignatureException(method, isStepHook);
        }

        // Validate first parameter (Scenario)
        if (parameterTypes.length >= 1) {
            Class<?> parameterType = parameterTypes[0];
            if (!(Object.class.equals(parameterType) || io.cucumber.java.Scenario.class.equals(parameterType))) {
                throw createInvalidSignatureException(method, isStepHook);
            }
        }

        // Validate second parameter (Step) - only for step hooks
        if (parameterTypes.length == 2) {
            Class<?> parameterType = parameterTypes[1];
            if (!(Object.class.equals(parameterType) || io.cucumber.java.Step.class.equals(parameterType))) {
                throw createInvalidSignatureException(method, isStepHook);
            }
        }

        Type returnType = method.getGenericReturnType();
        if (!Void.class.equals(returnType) && !void.class.equals(returnType)) {
            throw createInvalidSignatureException(method, isStepHook);
        }
        return method;
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method, boolean isStepHook) {
        InvalidMethodSignatureException.InvalidMethodSignatureExceptionBuilder exceptionBuilder = builder(method)
                .addAnnotation(Before.class)
                .addAnnotation(After.class)
                .addAnnotation(BeforeStep.class)
                .addAnnotation(AfterStep.class)
                .addSignature("public void before_or_after(io.cucumber.java.Scenario scenario)")
                .addSignature("public void before_or_after()");

        if (isStepHook) {
            exceptionBuilder.addSignature(
                "public void before_or_after_step(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)");
        }

        return exceptionBuilder.build();
    }

    @Override
    public void execute(TestCaseState state) {
        execute(state, null);
    }

    @Override
    public void execute(TestCaseState state, io.cucumber.plugin.event.Step step) {
        Object[] args;
        int paramCount = method.getParameterTypes().length;

        if (paramCount == 0) {
            args = new Object[0];
        } else if (paramCount == 1) {
            args = new Object[] { new io.cucumber.java.Scenario(state) };
        } else {
            // 2 parameters: Scenario and Step
            args = new Object[] {
                    new io.cucumber.java.Scenario(state),
                    step != null ? new StepInfo(step) : null
            };
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

    @Override
    public Optional<HookType> getHookType() {
        return Optional.of(hookType);
    }
}
