package io.cucumber.java;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.runtime.Invoker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

final class GlueAdaptor {

    private final Lookup lookup;
    private final Glue glue;

    GlueAdaptor(Lookup lookup, Glue glue) {
        this.lookup = lookup;
        this.glue = glue;
    }

    void addDefinition(Method method, Annotation annotation) {
        if (annotation.annotationType().getAnnotation(StepDefinitionAnnotation.class) != null) {
            String expression = expression(annotation);
            long timeoutMillis = timeoutMillis(annotation);
            glue.addStepDefinition(new JavaStepDefinition(method, expression, timeoutMillis, lookup));
        } else if (annotation.annotationType().equals(Before.class)) {
            Before before = (Before) annotation;
            String tagExpression = before.value();
            long timeout = before.timeout();
            glue.addBeforeHook(new JavaHookDefinition(method, tagExpression, before.order(), timeout, lookup));
        } else if (annotation.annotationType().equals(After.class)) {
            After after = (After) annotation;
            String tagExpression = after.value();
            long timeout = after.timeout();
            glue.addAfterHook(new JavaHookDefinition(method, tagExpression, after.order(), timeout, lookup));
        } else if (annotation.annotationType().equals(BeforeStep.class)) {
            BeforeStep beforeStep = (BeforeStep) annotation;
            String tagExpression = beforeStep.value();
            long timeout = beforeStep.timeout();
            glue.addBeforeStepHook(new JavaHookDefinition(method, tagExpression, beforeStep.order(), timeout, lookup));
        } else if (annotation.annotationType().equals(AfterStep.class)) {
            AfterStep afterStep = (AfterStep) annotation;
            String tagExpression = afterStep.value();
            long timeout = afterStep.timeout();
            glue.addAfterStepHook(new JavaHookDefinition(method, tagExpression, afterStep.order(), timeout, lookup));
        } else if (annotation.annotationType().equals(ParameterType.class)) {
            ParameterType parameterType = (ParameterType) annotation;
            String pattern = parameterType.value();
            String name = parameterType.name();
            boolean useForSnippets = parameterType.useForSnippets();
            boolean preferForRegexMatch = parameterType.preferForRegexMatch();
            glue.addParameterType(new JavaParameterTypeDefinition(name, pattern, method, useForSnippets, preferForRegexMatch, lookup));
        } else if (annotation.annotationType().equals(DataTableType.class)) {
            glue.addDataTableType(new JavaDataTableTypeDefinition(method, lookup));
        } else if (annotation.annotationType().equals(DefaultParameterTransformer.class)) {
            glue.addDefaultParameterTransformer(new JavaDefaultParameterTransformerDefinition(method, lookup));
        } else if (annotation.annotationType().equals(DefaultDataTableEntryTransformer.class)) {
            glue.addDefaultDataTableEntryTransformer(new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
        } else if (annotation.annotationType().equals(DefaultDataTableCellTransformer.class)) {
            glue.addDefaultDataTableCellTransformer(new JavaDefaultDataTableCellTransformerDefinition(method, lookup));
        }
    }

    private static String expression(Annotation annotation) {
        try {
            Method expressionMethod = annotation.getClass().getMethod("value");
            return (String) Invoker.invoke(annotation, expressionMethod);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private static long timeoutMillis(Annotation annotation) {
        try {
            Method regexpMethod = annotation.getClass().getMethod("timeout");
            return (Long) Invoker.invoke(annotation, regexpMethod);
        } catch (Throwable throwable) {
            throw new IllegalStateException(throwable);
        }
    }


}
