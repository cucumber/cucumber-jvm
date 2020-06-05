package io.cucumber.java;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;

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
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (annotationType.getAnnotation(StepDefinitionAnnotation.class) != null) {
            String expression = expression(annotation);
            glue.addStepDefinition(new JavaStepDefinition(method, expression, lookup));
        } else if (annotationType.equals(Before.class)) {
            Before before = (Before) annotation;
            String tagExpression = before.value();
            glue.addBeforeHook(new JavaHookDefinition(method, tagExpression, before.order(), lookup));
        } else if (annotationType.equals(After.class)) {
            After after = (After) annotation;
            String tagExpression = after.value();
            glue.addAfterHook(new JavaHookDefinition(method, tagExpression, after.order(), lookup));
        } else if (annotationType.equals(BeforeStep.class)) {
            BeforeStep beforeStep = (BeforeStep) annotation;
            String tagExpression = beforeStep.value();
            glue.addBeforeStepHook(new JavaHookDefinition(method, tagExpression, beforeStep.order(), lookup));
        } else if (annotationType.equals(AfterStep.class)) {
            AfterStep afterStep = (AfterStep) annotation;
            String tagExpression = afterStep.value();
            glue.addAfterStepHook(new JavaHookDefinition(method, tagExpression, afterStep.order(), lookup));
        } else if (annotationType.equals(ParameterType.class)) {
            ParameterType parameterType = (ParameterType) annotation;
            String pattern = parameterType.value();
            String name = parameterType.name();
            boolean useForSnippets = parameterType.useForSnippets();
            boolean preferForRegexMatch = parameterType.preferForRegexMatch();
            boolean useRegexpMatchAsStrongTypeHint = parameterType.useRegexpMatchAsStrongTypeHint();
            glue.addParameterType(new JavaParameterTypeDefinition(name, pattern, method, useForSnippets,
                preferForRegexMatch, useRegexpMatchAsStrongTypeHint, lookup));
        } else if (annotationType.equals(DataTableType.class)) {
            DataTableType dataTableType = (DataTableType) annotation;
            glue.addDataTableType(
                new JavaDataTableTypeDefinition(method, lookup, dataTableType.replaceWithEmptyString()));
        } else if (annotationType.equals(DefaultParameterTransformer.class)) {
            glue.addDefaultParameterTransformer(new JavaDefaultParameterTransformerDefinition(method, lookup));
        } else if (annotationType.equals(DefaultDataTableEntryTransformer.class)) {
            DefaultDataTableEntryTransformer transformer = (DefaultDataTableEntryTransformer) annotation;
            boolean headersToProperties = transformer.headersToProperties();
            String[] replaceWithEmptyString = transformer.replaceWithEmptyString();
            glue.addDefaultDataTableEntryTransformer(new JavaDefaultDataTableEntryTransformerDefinition(method, lookup,
                headersToProperties, replaceWithEmptyString));
        } else if (annotationType.equals(DefaultDataTableCellTransformer.class)) {
            DefaultDataTableCellTransformer cellTransformer = (DefaultDataTableCellTransformer) annotation;
            String[] emptyPatterns = cellTransformer.replaceWithEmptyString();
            glue.addDefaultDataTableCellTransformer(
                new JavaDefaultDataTableCellTransformerDefinition(method, lookup, emptyPatterns));
        } else if (annotationType.equals(DocStringType.class)) {
            DocStringType docStringType = (DocStringType) annotation;
            String contentType = docStringType.contentType();
            glue.addDocStringType(new JavaDocStringTypeDefinition(contentType, method, lookup));
        }
    }

    private static String expression(Annotation annotation) {
        try {
            Method expressionMethod = annotation.getClass().getMethod("value");
            return (String) Invoker.invoke(annotation, expressionMethod);
        } catch (NoSuchMethodException e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
    }

}
