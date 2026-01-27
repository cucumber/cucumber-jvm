package io.cucumber.java;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;

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
        Type returnType = method.getGenericReturnType();
        if (!Void.class.equals(returnType) && !void.class.equals(returnType)) {
            throw createInvalidSignatureException(method, hookType);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();

        switch (hookType) {
            case BEFORE_STEP:
            case AFTER_STEP:
                validateStepHookParameters(method, parameterTypes, hookType);
                break;
            case BEFORE:
            case AFTER:
                validateScenarioHookParameters(method, parameterTypes, hookType);
                break;
            default:
                throw new IllegalArgumentException("Unknown hook type: " + hookType);
        }

        return method;
    }

    private static void validateScenarioHookParameters(Method method, Class<?>[] parameterTypes, HookType hookType) {
        // @Before and @After hooks: 0 or 1 parameter (Scenario)
        if (parameterTypes.length > 1) {
            throw createInvalidSignatureException(method, hookType);
        }
        if (parameterTypes.length == 1) {
            Class<?> parameterType = parameterTypes[0];
            if (!Object.class.equals(parameterType) && !io.cucumber.java.Scenario.class.equals(parameterType)) {
                throw createInvalidSignatureException(method, hookType);
            }
        }
    }

    private static void validateStepHookParameters(Method method, Class<?>[] parameterTypes, HookType hookType) {
        // @BeforeStep and @AfterStep hooks: 0, 1, or 2 parameters (Scenario,
        // Step)
        if (parameterTypes.length > 2) {
            throw createInvalidSignatureException(method, hookType);
        }
        if (parameterTypes.length >= 1) {
            Class<?> firstParam = parameterTypes[0];
            if (!Object.class.equals(firstParam) && !io.cucumber.java.Scenario.class.equals(firstParam)) {
                throw createInvalidSignatureException(method, hookType);
            }
        }
        if (parameterTypes.length == 2) {
            Class<?> secondParam = parameterTypes[1];
            if (!Object.class.equals(secondParam) && !io.cucumber.java.Step.class.equals(secondParam)) {
                throw createInvalidSignatureException(method, hookType);
            }
        }
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method, HookType hookType) {
        InvalidMethodSignatureException.InvalidMethodSignatureExceptionBuilder exceptionBuilder = builder(method);

        switch (hookType) {
            case BEFORE:
                exceptionBuilder.addAnnotation(Before.class);
                break;
            case AFTER:
                exceptionBuilder.addAnnotation(After.class);
                break;
            case BEFORE_STEP:
                exceptionBuilder.addAnnotation(BeforeStep.class);
                break;
            case AFTER_STEP:
                exceptionBuilder.addAnnotation(AfterStep.class);
                break;
        }

        exceptionBuilder.addSignature("public void hook()")
                .addSignature("public void hook(io.cucumber.java.Scenario scenario)");

        if (hookType == HookType.BEFORE_STEP || hookType == HookType.AFTER_STEP) {
            exceptionBuilder.addSignature(
                "public void hook(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)");
        }

        return exceptionBuilder.build();
    }

    @Override
    public void execute(TestCaseState state) {
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
                    state.geCurrentPickleStep().map(Step::new).orElse(null)
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
