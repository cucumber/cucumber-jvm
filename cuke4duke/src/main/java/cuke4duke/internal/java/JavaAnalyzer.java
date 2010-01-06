package cuke4duke.internal.java;

import cuke4duke.*;
import cuke4duke.internal.jvmclass.ClassAnalyzer;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class JavaAnalyzer implements ClassAnalyzer {
    private final MethodFormat methodFormat;

    public JavaAnalyzer() {
        this.methodFormat = new MethodFormat(System.getProperty("cuke4duke.methodFormat", "%c.%m(%a)"));
    }

    public void populateStepDefinitionsAndHooks(ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable {
        for(Method method: getOrderedMethods(classLanguage)) {
            registerBeforeMaybe(method, classLanguage);
            registerAfterMaybe(method, classLanguage);
            registerStepDefinitionMaybe(method, classLanguage);
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
        for(Class<?> clazz : classLanguage.getClasses()) {
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
            classLanguage.addBeforeHook(new JavaHook(classLanguage, method, method.getAnnotation(Before.class).value()));
        }
    }

    private void registerAfterMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(After.class)) {
            classLanguage.addAfterHook(new JavaHook(classLanguage, method, method.getAnnotation(After.class).value()));
        }
    }

    private void registerStepDefinitionMaybe(Method method, ClassLanguage classLanguage) throws Throwable {
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
            classLanguage.addStepDefinition(new JavaStepDefinition(classLanguage, method, regexp, methodFormat));
        }
    }
}
