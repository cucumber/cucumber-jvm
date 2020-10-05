package io.cucumber.java;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.TestStep;

import java.lang.reflect.Method;
import java.util.Optional;

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
        Class<?>[] acceptedTypes = new Class<?>[] { io.cucumber.java.Scenario.class };

        if (method.isAnnotationPresent(BeforeStep.class) || method.isAnnotationPresent(AfterStep.class)) {
            acceptedTypes = new Class<?>[] { io.cucumber.java.Scenario.class, io.cucumber.core.gherkin.Step.class };
        }

        if (parameterTypes.length > 0) {
            if (parameterTypes.length > acceptedTypes.length) {
                throw createInvalidSignatureException(method);
            }
            for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
                checkParameter(method, parameterIndex, parameterTypes, acceptedTypes);
            }
        }

        return method;
    }

    private static void checkParameter(Method method, int index, Class<?>[] parameterTypes, Class<?>[] acceptedTypes) {
        Class<?> parameterType = parameterTypes[index];
        Class<?> acceptedType = acceptedTypes[index];
        if (!(Object.class.equals(parameterType) || acceptedType.equals(parameterType))) {
            throw createInvalidSignatureException(method);
        }
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        boolean annotated = false;
        InvalidMethodSignatureException.InvalidMethodSignatureExceptionBuilder builder = builder(method);
        if (method.isAnnotationPresent(Before.class) || method.isAnnotationPresent(After.class)) {
            builder
                    .addAnnotation(Before.class)
                    .addAnnotation(After.class)
                    .addSignature("public void before_or_after(io.cucumber.java.Scenario scenario)")
                    .addSignature("public void before_or_after()");
            annotated = true;
        }
        if (method.isAnnotationPresent(BeforeStep.class) || method.isAnnotationPresent(AfterStep.class)) {
            builder
                    .addAnnotation(BeforeStep.class)
                    .addAnnotation(AfterStep.class)
                    .addSignature(
                        "public void before_or_after_step(io.cucumber.java.Scenario scenario, io.cucumber.core.gherkin.Step step)")
                    .addSignature("public void before_or_after_step(io.cucumber.java.Scenario scenario)")
                    .addSignature("public void before_or_after_step()");
            annotated = true;
        }
        if (!annotated) {
            throw new IllegalArgumentException(
                "Method should be annotated with one of: @Before, @After, @BeforeStep, @AfterStep");
        }
        return builder.build();
    }

    @Override
    public void execute(TestCaseState state) {
        Object[] args;
        if (method.getParameterTypes().length == 2) {
            Step relatedStep = null;
            Optional<TestStep> currentTestStep = state.getCurrentTestStep();
            if (currentTestStep.isPresent() && currentTestStep.get() instanceof HookTestStep) {
                TestStep relatedTestStep = (((HookTestStep) currentTestStep.get())).getRelatedTestStep();
                if (null != relatedTestStep) {
                    relatedStep = ((PickleStepTestStep) relatedTestStep).getStep();
                }
            }
            args = new Object[] { new io.cucumber.java.Scenario(state), relatedStep };
        } else if (method.getParameterTypes().length == 1) {
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
