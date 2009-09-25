package cuke4duke.internal.java;

import cuke4duke.*;
import cuke4duke.internal.jvmclass.ClassAnalyzer;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.Hook;
import cuke4duke.internal.language.StepDefinition;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class JavaAnalyzer implements ClassAnalyzer {

    @Override
    public void populateStepDefinitionsAndHooksFor(Class<?> clazz, ObjectFactory objectFactory, List<Hook> befores, List<StepDefinition> stepDefinitions, List<Hook> afters) {
        for (Method method : clazz.getMethods()) {
            registerBeforeMaybe(method, befores, objectFactory);
            registerStepDefinitionMaybe(method, stepDefinitions, objectFactory);
            registerAfterMaybe(method, afters, objectFactory);
        }
    }

    private void registerBeforeMaybe(Method method, List<Hook> befores, ObjectFactory objectFactory) {
        if (method.isAnnotationPresent(Before.class)) {
            List<String> tagNames = Arrays.asList(method.getAnnotation(Before.class).value().split(","));
            befores.add(new JavaHook(tagNames, method, objectFactory));
        }
    }

    private void registerStepDefinitionMaybe(Method method, List<StepDefinition> stepDefinitions, ObjectFactory objectFactory) {
        String regexpString = null;
        if (method.isAnnotationPresent(Given.class)) {
            regexpString = method.getAnnotation(Given.class).value();
        } else if (method.isAnnotationPresent(When.class)) {
            regexpString = method.getAnnotation(When.class).value();
        } else if (method.isAnnotationPresent(Then.class)) {
            regexpString = method.getAnnotation(Then.class).value();
        }
        if (regexpString != null) {
            Pattern regexp = Pattern.compile(regexpString);
            stepDefinitions.add(new JavaStepDefinition(objectFactory, method, regexp));
        }
    }


    private void registerAfterMaybe(Method method, List<Hook> afters, ObjectFactory objectFactory) {
        if (method.isAnnotationPresent(After.class)) {
            List<String> tagNames = Arrays.asList(method.getAnnotation(After.class).value().split(","));
            afters.add(new JavaHook(tagNames, method, objectFactory));
        }
    }
}
