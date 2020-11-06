package io.cucumber.java;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.HookStep;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.PickleStep;
import io.cucumber.core.backend.TestCaseState;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        if (parameterTypes.length > 0) {
            Class<?>[] annotationTypes = getRelevantMethodAnnotations(method.getAnnotations());
            for (Class<?> parameterType : parameterTypes) {
                checkParameter(method, parameterType, annotationTypes);
            }
        }
        return method;
    }

    private static Class<?>[] getRelevantMethodAnnotations(Annotation[] annotations) {
        if (null == annotations || annotations.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] relevant = null;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (Before.class.equals(annotationType) || After.class.equals(annotationType)) {
                return new Class<?>[] { Before.class, After.class };
            }
            if (BeforeStep.class.equals(annotationType) || AfterStep.class.equals(annotationType)) {
                relevant = new Class<?>[] { BeforeStep.class, AfterStep.class };
            }
        }
        return relevant;
    }

    private static Set<Class<?>> getAcceptedTypes(Class<?> annotationType) {
        if (null == annotationType) {
            return Collections.emptySet();
        }
        Set<Class<?>> accepted = new HashSet<>();
        if (Before.class.equals(annotationType) || After.class.equals(annotationType)) {
            accepted.add(Scenario.class);
        }
        if (BeforeStep.class.equals(annotationType) || AfterStep.class.equals(annotationType)) {
            accepted.addAll(Arrays.asList(Scenario.class, Step.class));
        }
        return accepted;
    }

    private static void checkParameter(Method method, Class<?> parameterType, Class<?>[] annotationTypes) {
        for (Class<?> annotationType : annotationTypes) {
            Set<Class<?>> acceptedTypes = getAcceptedTypes(annotationType);
            if (!(Object.class.equals(parameterType) || acceptedTypes.contains(parameterType))) {
                throw createInvalidSignatureException(method, annotationTypes, acceptedTypes);
            }
        }
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(
            Method method,
            Class<?>[] methodAnnotations,
            Set<Class<?>> acceptedTypes
    ) {
        InvalidMethodSignatureException.InvalidMethodSignatureExceptionBuilder builder = builder(method);
        if (methodAnnotations.length == 0) {
            throw new IllegalArgumentException(
                "Method should be annotated with one of: @Before, @After, @BeforeStep, @AfterStep");
        }
        builder.addAnnotations(methodAnnotations);
        String methodName = "public void before_or_after";
        if (acceptedTypes.contains(Step.class)) {
            methodName += "_step";
            builder.addSignature(methodName + "(io.cucumber.java.Scenario scenario, io.cucumber.java.Step step)");
            builder.addSignature(methodName + "(io.cucumber.java.Step step, io.cucumber.java.Scenario scenario)");
            builder.addSignature(methodName + "(io.cucumber.java.Step step)");
        }
        builder.addSignature(methodName + "(io.cucumber.java.Scenario scenario)");
        builder.addSignature(methodName + "()");
        return builder.build();
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

        HookStep hookStep = Optional.of(step)
                .filter(HookStep.class::isInstance)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Current TestStep should be a %s instead of a %s",
                        HookStep.class.getSimpleName(), currentTestStep.get().getClass().getSimpleName())));
                                
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
