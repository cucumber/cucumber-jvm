package cucumber.runtime.java.spring;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;
import org.springframework.stereotype.Component;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Spring based implementation of ObjectFactory.
 * <p>
 * Application beans are accessible from the step definitions using autowiring
 * (with annotations).
 * <p>
 * SpringFactory uses CucumberTestContextManager to manage the spring context. The step definitions are added to the
 * TestContextManagers context and the context is reloaded for each scenario.
 * <p>
 * The spring context can be configured by:
 * <ul>
 * <li>Annotating one step definition with: @{@link ContextConfiguration},  @{@link ContextHierarchy}
 * or @{@link BootstrapWith}. This step definition can also be annotated
 * with @{@link org.springframework.test.context.web.WebAppConfiguration}
 * or @{@link org.springframework.test.annotation.DirtiesContext} annotation.
 * </li>
 * <li>If no step definition class with @ContextConfiguration or @ContextHierarchy
 * is found, it will try to load cucumber.xml from the classpath.</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>
 * Step definitions should not be annotated with @{@link Component} or other annotations that mark it as eligible for
 * detection by classpath scanning. When a step definition class is annotated by @Component or an annotation that has
 * the @Component stereotype an exception will be thrown
 * </li>
 * <li>
 * If more that one step definition class is used to configure the spring context an exception will be thrown.
 * </li>
 * </ul>
 */
public class SpringFactory implements ObjectFactory {

    private final Collection<Class<?>> stepClasses = new HashSet<>();
    private Class<?> stepClassWithSpringContext = null;
    private Map<Class<?>, Object> instances;
    private CucumberTestContextManager testContextManager;

    private static void checkOneNoDefaultConstructor(Class<?> stepClass) {
        Constructor<?>[] constructors = stepClass.getConstructors();
        if (constructors.length != 1 || constructors[0].getParameterTypes().length != 0) {
            throw new CucumberException("" +
                "Step definition class '" + stepClass.getName() + "' " +
                "should have exactly one public zero-argument constructor."
            );
        }
    }

    private static void checkNoComponentAnnotations(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if (hasComponentAnnotation(annotation)) {
                throw new CucumberException(String.format("" +
                        "Glue class %1$s was annotated with @%2$s; marking it as a candidate for auto-detection by " +
                        "Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by " +
                        "spring may lead to duplicate bean definitions. Please remove the @%2$s annotation",
                    type.getName(),
                    annotation.annotationType().getSimpleName()));
            }
        }
    }

    private static boolean hasComponentAnnotation(Annotation annotation) {
        return hasAnnotation(annotation, Collections.<Class<? extends Annotation>>singleton(Component.class));
    }

    private static boolean hasAnnotation(Annotation annotation, Collection<Class<? extends Annotation>> desired) {
        Set<Class<? extends Annotation>> seen = new HashSet<>();
        Deque<Class<? extends Annotation>> toCheck = new ArrayDeque<>();
        toCheck.add(annotation.annotationType());

        while (!toCheck.isEmpty()) {
            Class<? extends Annotation> annotationType = toCheck.pop();
            if (desired.contains(annotationType)) {
                return true;
            }

            seen.add(annotationType);
            for (Annotation annotationTypesAnnotations : annotationType.getAnnotations()) {
                if (!seen.contains(annotationTypesAnnotations.annotationType())) {
                    toCheck.add(annotationTypesAnnotations.annotationType());
                }
            }

        }
        return false;
    }

    private static boolean dependsOnSpringContext(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if (annotatedWithSupportedSpringRootTestAnnotations(annotation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean annotatedWithSupportedSpringRootTestAnnotations(Annotation type) {
        return hasAnnotation(type, asList(
            ContextConfiguration.class,
            ContextHierarchy.class,
            BootstrapWith.class));
    }

    @Override
    public boolean addClass(final Class<?> stepClass) {
        if (stepClasses.contains(stepClass)) {
            return true;
        }

        checkOneNoDefaultConstructor(stepClass);
        checkNoComponentAnnotations(stepClass);

        if (dependsOnSpringContext(stepClass)) {
            if (stepClassWithSpringContext != null) {
                throw new CucumberException(String.format("" +
                    "Glue class %1$s and %2$s both attempt to configure the spring context. Please ensure only one " +
                    "glue class configures the spring context", stepClass, stepClassWithSpringContext));
            }
            stepClassWithSpringContext = stepClass;
        }

        stepClasses.add(stepClass);
        return true;
    }

    @Override
    public void start() {
        try {
            Method dummyTestMethod = SpringFactory.class.getMethod("cucumberDoesNotHaveTestMethods");
            testContextManager = new CucumberTestContextManager(stepClassWithSpringContext, stepClasses);
            testContextManager.beforeTestClass();
            instances = instantiateStepDefinitions();
            testContextManager.prepareTestInstance(instances);
            testContextManager.beforeTestMethod(instances, dummyTestMethod);
            testContextManager.beforeTestExecution(instances, dummyTestMethod);
        } catch (Exception e) {
            throw new CucumberException("Failed to start cucumber-spring factory",e);
        }
    }

    private Map<Class<?>, Object> instantiateStepDefinitions() throws ReflectiveOperationException {
        Map<Class<?>, Object> typeToInstance = new HashMap<>();
        for (Class<?> stepClass : stepClasses) {
            Object instance = stepClass.getConstructor().newInstance();
            typeToInstance.put(stepClass, instance);
        }
        return typeToInstance;
    }

    @Override
    public void stop() {
        try {
            Method dummyTestMethod = SpringFactory.class.getMethod("cucumberDoesNotHaveTestMethods");
            testContextManager.afterTestExecution(instances, dummyTestMethod, null);
            testContextManager.afterTestMethod(instances, dummyTestMethod, null);
            testContextManager.afterTestClass();
            instances = null;
            testContextManager = null;
        } catch (Exception e) {
            throw new CucumberException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(final Class<T> type) {
        return (T) instances.get(type);
    }

    /**
     * Dummy method to use in before/after test methods.
     *
     * Some {@link org.springframework.test.context.TestExecutionListener}s use annotations
     * on the test method to prepare the test context. However Cucumber does not have any test methods.
     *
     * This method can stand in for that.
     */
    @SuppressWarnings("WeakerAccess")
    public void cucumberDoesNotHaveTestMethods() {
        // No-op
    }
}

