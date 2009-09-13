package cuke4duke.internal.java;

import cuke4duke.*;
import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.jvmclass.HookAndStepDefinitionRegistrar;
import cuke4duke.internal.jvmclass.ClassLanguage;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class JavaRegistrar implements HookAndStepDefinitionRegistrar {
    public void registerHooksAndStepDefinitionsFor(Class<?> clazz, ClassLanguage classLanguage) {
        for (Method method : clazz.getMethods()) {
            registerStepDefinitionMaybe(method, classLanguage);
            registerBeforeMaybe(method, classLanguage);
            registerAfterMaybe(method, classLanguage);
        }
    }

    private void registerStepDefinitionMaybe(Method method, ClassLanguage classLanguage) {
        String regexpString = null;
        if (method.isAnnotationPresent(Given.class)) {
            regexpString = method.getAnnotation(Given.class).value();
        } else if (method.isAnnotationPresent(When.class)) {
            regexpString = method.getAnnotation(When.class).value();
        } else if (method.isAnnotationPresent(Then.class)) {
            regexpString = method.getAnnotation(Then.class).value();
        }
        if (regexpString != null) {
            StepDefinition stepDefinition = new JavaStepDefinition(classLanguage, method, regexpString);
            classLanguage.addStepDefinition(stepDefinition);
        }
    }

    private void registerBeforeMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(Before.class)) {
            List<String> tagNames = Arrays.asList(method.getAnnotation(Before.class).value().split(","));
            classLanguage.addHook("before", new JavaHook(tagNames, method, classLanguage));
        }
    }

    private void registerAfterMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(After.class)) {
            List<String> tagNames = Arrays.asList(method.getAnnotation(After.class).value().split(","));
            classLanguage.addHook("after", new JavaHook(tagNames, method, classLanguage));
        }
    }
}
