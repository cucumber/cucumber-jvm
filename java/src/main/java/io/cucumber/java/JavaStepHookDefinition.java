package io.cucumber.java;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.HookStep;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.PickleStep;
import io.cucumber.core.backend.TestCaseState;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.cucumber.java.InvalidMethodSignatureException.builder;
import static java.util.Objects.requireNonNull;

final class JavaStepHookDefinition extends AbstractGlueDefinition implements HookDefinition {

    private final String tagExpression;
    private final int order;

    JavaStepHookDefinition(Method method, String tagExpression, int order, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.tagExpression = requireNonNull(tagExpression, "tag-expression may not be null");
        this.order = order;
    }

    private static Method requireValidMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 2) {
            throw createInvalidSignatureException(method);
        }
        if (parameterTypes.length == 2) {
            Class<?> parameterType1 = parameterTypes[0];
            Class<?> parameterType2 = parameterTypes[1];
            if (!(isObjectOrClass(parameterType1, Scenario.class) && isObjectOrClass(parameterType2, Step.class) ||
                    isObjectOrClass(parameterType1, Step.class) && isObjectOrClass(parameterType2, Scenario.class))) {
                throw createInvalidSignatureException(method);
            }
        }
        if (parameterTypes.length == 1) {
            Class<?> parameterType1 = parameterTypes[0];
            if (!(isObjectOrClass(parameterType1, Scenario.class) || isObjectOrClass(parameterType1, Step.class))) {
                throw createInvalidSignatureException(method);
            }
        }
        return method;
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
                .addAnnotation(BeforeStep.class)
                .addAnnotation(AfterStep.class)
                .addSignature(
                    "public void before_or_after_step(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)")
                .addSignature(
                    "public void before_or_after_step(io.cucumber.java.Step step, io.cucumber.java.Scenario scenario)")
                .addSignature("public void before_or_after_step(io.cucumber.java.Scenario scenario)")
                .addSignature("public void before_or_after_step(io.cucumber.java.Step step)")
                .addSignature("public void before_or_after_step()")
                .build();
    }

    private static boolean isObjectOrClass(Object type, Class<?> clazz) {
        return null != type && (Object.class.equals(type) || clazz.equals(type));
    }

    @Override
    public void execute(TestCaseState state) {
        List<Object> parameters = new ArrayList<>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterType.equals(Scenario.class)) {
                parameters.add(new Scenario(state));
            }
            if (parameterType.equals(Step.class)) {
                PickleStep pickleStep = getCurrentPickleStep(state);
                parameters.add(new Step(pickleStep));
            }
        }
        invokeMethod(parameters.toArray());
    }

    private PickleStep getCurrentPickleStep(TestCaseState state) {
        io.cucumber.core.backend.Step step = state.getCurrentTestStep()
                .orElseThrow(() -> new IllegalStateException("No current TestStep was found in TestCaseState"));
        HookStep hookStep = (HookStep) Optional.of(step)
                .filter(HookStep.class::isInstance)
                .orElseThrow(
                    () -> new IllegalArgumentException(String.format("Current TestStep should be a %s instead of a %s",
                        HookStep.class.getSimpleName(), step.getClass().getSimpleName())));
        return Optional.of(hookStep)
                .map(HookStep::getRelatedStep)
                .orElseThrow(() -> new IllegalStateException("Current HookStep has no related PickleStep"));
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
