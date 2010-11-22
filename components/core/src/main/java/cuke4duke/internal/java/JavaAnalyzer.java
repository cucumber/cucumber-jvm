package cuke4duke.internal.java;

import cucumber.annotation.After;
import cucumber.annotation.Order;
import cucumber.runtime.java.CucumberAnnotation;
import cucumber.runtime.java.MethodFormat;
import cucumber.annotation.Before;
import cucumber.annotation.Transform;
import cuke4duke.internal.Utils;
import cuke4duke.internal.jvmclass.ClassAnalyzer;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cucumber.runtime.java.ObjectFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class JavaAnalyzer implements ClassAnalyzer {
    private final MethodFormat methodFormat;
    private static final String[] NO_TAGS = new String[0];

    public JavaAnalyzer() {
        this.methodFormat = new MethodFormat(System.getProperty("cuke4duke.methodFormat", "%c.%m(%a)"));
    }

    public void populateStepDefinitionsAndHooks(ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable {
        for (Method method : getOrderedMethods(classLanguage)) {
            registerBeforeMaybe(method, classLanguage);
            registerAfterMaybe(method, classLanguage);
            registerStepDefinitionsFromAnnotations(method, classLanguage);
            registerTransformMaybe(method, classLanguage);
        }
    }

    private void registerTransformMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(Transform.class)) {
            classLanguage.addTransform(method.getReturnType(), new JavaTransform(classLanguage, method));
        }
    }

    public Class<?>[] alwaysLoad() {
        return new Class<?>[0];
    }

    private List<Method> getOrderedMethods(ClassLanguage classLanguage) {
        Set<Method> methods = new HashSet<Method>();
        for (Class<?> clazz : classLanguage.getClasses()) {
            methods.addAll(Arrays.asList(clazz.getMethods()));
        }
        List<Method> sortedMethods = new ArrayList<Method>(methods);
        Collections.sort(sortedMethods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return order(m1) - order(m2);
            }

            private int order(Method m) {
                Order order = m.getAnnotation(Order.class);
                return (order == null) ? Integer.MAX_VALUE : order.value();
            }
        });
        return sortedMethods;
    }

    private void registerBeforeMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(Before.class)) {
            String[] tagExpressions = method.getAnnotation(Before.class).value();
            if ("".equals(tagExpressions[0])) {
                tagExpressions = NO_TAGS;
            }
            classLanguage.addBeforeHook(new JavaHook(classLanguage, method, Arrays.asList(tagExpressions)));
        }
    }

    private void registerAfterMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(After.class)) {
            String[] tagExpressions = method.getAnnotation(After.class).value();
            if ("".equals(tagExpressions[0])) {
                tagExpressions = NO_TAGS;
            }
            classLanguage.addAfterHook(new JavaHook(classLanguage, method, Arrays.asList(tagExpressions)));
        }
    }

    private void registerStepDefinitionsFromAnnotations(Method method, ClassLanguage classLanguage) throws Throwable {
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(CucumberAnnotation.class)) {
                Locale locale = Utils.localeFor(annotation.annotationType().getAnnotation(CucumberAnnotation.class).value());
                Method regexpMethod = annotation.getClass().getMethod("value");
                String regexpString = (String) regexpMethod.invoke(annotation);
                if (regexpString != null) {
                    Pattern regexp = Pattern.compile(regexpString);
//                    classLanguage.addStepDefinition(new JavaStepDefinition(classLanguage, method, regexp, methodFormat, locale));
                }
            }
        }
    }
}
