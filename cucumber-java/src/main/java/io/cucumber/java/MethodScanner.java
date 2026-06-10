package io.cucumber.java;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import static io.cucumber.core.resource.ClasspathSupport.classPathScanningExplanation;
import static io.cucumber.java.InvalidMethodException.annotatedMethodInParentClass;
import static io.cucumber.java.InvalidMethodException.invalidModifier;
import static io.cucumber.java.Invoker.invoke;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

final class MethodScanner {

    private static final Logger log = LoggerFactory.getLogger(MethodScanner.class);

    private MethodScanner() {
    }

    static void scan(Class<?> aClass, BiConsumer<Method, Annotation> consumer) {
        // prevent unnecessary checking of Object methods
        if (Object.class.equals(aClass)) {
            return;
        }

        if (!isInstantiable(aClass)) {
            return;
        }
        for (Method method : safelyGetDeclaredMethods(aClass)) {
            scan(method, validate().andThen(consumer));
        }

        requireNoGlueDefinitionsInSuperClass(aClass);
    }

    private static BiConsumer<Method, Annotation> validate() {
        return (method, annotation) -> {
            if (!isConcreteNonPrivate(method)) {
                throw invalidModifier(method);
            }
        };
    }

    private static void requireNoGlueDefinitionsInSuperClass(Class<?> aClass) {
        for (Class<?> superclass = aClass.getSuperclass(); //
                !Object.class.equals(superclass); //
                superclass = superclass.getSuperclass()) {
            for (Method method : safelyGetDeclaredMethods(superclass)) {
                scan(method, (candiateMethod, annotation) -> {
                    throw annotatedMethodInParentClass(candiateMethod, aClass);
                });
            }
        }
    }

    private static Method[] safelyGetDeclaredMethods(Class<?> aClass) {
        try {
            return aClass.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            log.trace(e,
                () -> "Failed to load declared methods of class '" + aClass.getName() + "'.\n"
                        + classPathScanningExplanation());
        }
        return new Method[0];
    }

    private static boolean isConcreteNonPrivate(Method aMethod) {
        return !isPrivate(aMethod.getModifiers())
                && !isAbstract(aMethod.getModifiers());
    }

    private static boolean isInstantiable(Class<?> clazz) {
        return !isPrivate(clazz.getModifiers())
                && !isAbstract(clazz.getModifiers())
                && (isStatic(clazz.getModifiers()) || clazz.getEnclosingClass() == null);
    }

    private static void scan(Method method, BiConsumer<Method, Annotation> consumer) {
        // prevent unnecessary checking of Object methods
        if (Object.class.equals(method.getDeclaringClass())) {
            return;
        }

        // exclude bridge methods: when a class implements a method
        // from the interface but specializes the return type, two methods will
        // be generated. One with the return type of the interface and one
        // with the specialized return type. The former is a bridge method.
        // Depending on the JVM, the method annotations are also applied to
        // the bridge method.
        if (method.isBridge()) {
            return;
        }

        scan(consumer, method, method.getAnnotations());
    }

    private static void scan(
            BiConsumer<Method, Annotation> consumer, Method method, Annotation[] methodAnnotations
    ) {
        for (Annotation annotation : methodAnnotations) {
            if (isHookAnnotation(annotation) || isStepDefinitionAnnotation(annotation)) {
                consumer.accept(method, annotation);
            } else if (isRepeatedStepDefinitionAnnotation(annotation)) {
                scan(consumer, method, repeatedAnnotations(annotation));
            }
        }
    }

    private static boolean isHookAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.equals(Before.class)
                || annotationClass.equals(BeforeAll.class)
                || annotationClass.equals(After.class)
                || annotationClass.equals(AfterAll.class)
                || annotationClass.equals(BeforeStep.class)
                || annotationClass.equals(AfterStep.class)
                || annotationClass.equals(ParameterType.class)
                || annotationClass.equals(DataTableType.class)
                || annotationClass.equals(DefaultParameterTransformer.class)
                || annotationClass.equals(DefaultDataTableEntryTransformer.class)
                || annotationClass.equals(DefaultDataTableCellTransformer.class)
                || annotationClass.equals(DocStringType.class);
    }

    private static boolean isStepDefinitionAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.getAnnotation(StepDefinitionAnnotation.class) != null;
    }

    private static boolean isRepeatedStepDefinitionAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.getAnnotation(StepDefinitionAnnotations.class) != null;
    }

    @SuppressWarnings("GetClassOnAnnotation")
    private static Annotation[] repeatedAnnotations(Annotation annotation) {
        try {
            Method expressionMethod = annotation.getClass().getMethod("value");
            return (Annotation[]) requireNonNull(invoke(annotation, expressionMethod));
        } catch (NoSuchMethodException e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
    }

}
