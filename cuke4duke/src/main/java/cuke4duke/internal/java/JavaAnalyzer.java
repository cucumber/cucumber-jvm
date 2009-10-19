package cuke4duke.internal.java;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import cuke4duke.After;
import cuke4duke.Before;
import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.Transform;
import cuke4duke.When;
import cuke4duke.internal.jvmclass.ClassAnalyzer;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;

public class JavaAnalyzer implements ClassAnalyzer {

    public void populateStepDefinitionsAndHooksFor(Class<?> clazz, ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable {
        for (Method method : clazz.getMethods()) {
            registerBeforeMaybe(method, classLanguage, objectFactory);
            registerStepDefinitionMaybe(method, classLanguage, objectFactory);
            registerAfterMaybe(method, classLanguage, objectFactory);
            registerTransformMaybe(method, classLanguage, objectFactory);
        }
    }

    public void addDefaultTransforms(ClassLanguage classLanguage, ObjectFactory objectFactory) {
        for (Method method : DefaultJavaTransforms.class.getMethods()) {
            registerTransformMaybe(method, classLanguage, objectFactory);
        }
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
            List<String> tagNames = Arrays.asList(method.getAnnotation(Before.class).value().split(","));
            classLanguage.addBeforeHook(new JavaHook(tagNames, method, objectFactory));
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
            List<String> tagNames = Arrays.asList(method.getAnnotation(After.class).value().split(","));
            classLanguage.addAfterHook(new JavaHook(tagNames, method, objectFactory));
        }
    }

}
