package cuke4duke.internal.java;

import cuke4duke.*;
import cuke4duke.internal.jvmclass.ClassAnalyzer;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class JavaAnalyzer implements ClassAnalyzer {

    public void populateStepDefinitionsAndHooksFor(Method method, ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable {
        registerBeforeMaybe(method, classLanguage, objectFactory);
        registerStepDefinitionMaybe(method, classLanguage, objectFactory);
        registerAfterMaybe(method, classLanguage, objectFactory);
        registerTransformMaybe(method, classLanguage, objectFactory);
    }

    private void registerTransformMaybe(Method method, ClassLanguage classLanguage, ObjectFactory objectFactory) {
        if (method.isAnnotationPresent(Transform.class)) {
            classLanguage.addTransform(method.getReturnType(), new JavaTransform(method, objectFactory));
        }
    }

    public Class<?>[] alwaysLoad() {
        return new Class<?>[0];
    }

    private void registerBeforeMaybe(Method method, ClassLanguage classLanguage, ObjectFactory objectFactory) {
        if (method.isAnnotationPresent(Before.class)) {
            classLanguage.addBeforeHook(new JavaHook(method.getAnnotation(Before.class).value(), method, objectFactory));
        }
    }

    private void registerStepDefinitionMaybe(Method method, ClassLanguage classLanguage, ObjectFactory objectFactory) throws Throwable {
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
            classLanguage.addStepDefinition(new JavaStepDefinition(classLanguage, objectFactory, method, regexp));
        }
    }

    private void registerAfterMaybe(Method method, ClassLanguage classLanguage, ObjectFactory objectFactory) {
        if (method.isAnnotationPresent(After.class)) {
            classLanguage.addAfterHook(new JavaHook(method.getAnnotation(After.class).value(), method, objectFactory));
        }
    }

}
