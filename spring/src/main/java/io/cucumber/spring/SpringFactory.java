package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import org.apiguardian.api.API;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestContextManager;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import static io.cucumber.spring.TestContextAdaptor.createApplicationContextAdaptor;
import static io.cucumber.spring.TestContextAdaptor.createTestContextManagerAdaptor;
import static java.util.Arrays.asList;

/**
 * Spring based implementation of ObjectFactory.
 * <p>
 * Application beans are accessible from the step definitions using autowiring
 * (with annotations).
 * <p>
 * SpringFactory uses TestContextManager to manage the spring context. The step definitions are added to the
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
@API(status = API.Status.STABLE)
public final class SpringFactory implements ObjectFactory {

    private static final String CUCUMBER_XML = "cucumber.xml";
    private final Collection<Class<?>> stepClasses = new HashSet<>();
    private Class<?> stepClassWithSpringContext = null;
    private TestContextAdaptor testContextAdaptor;

    private static void checkNoComponentAnnotations(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if (hasComponentAnnotation(annotation)) {
                throw new CucumberBackendException(String.format("" +
                        "Glue class %1$s was annotated with @%2$s; marking it as a candidate for auto-detection by " +
                        "Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by " +
                        "spring may lead to duplicate bean definitions. Please remove the @%2$s annotation",
                    type.getName(),
                    annotation.annotationType().getSimpleName()));
            }
        }
    }

    private static boolean hasComponentAnnotation(Annotation annotation) {
        return hasAnnotation(annotation, Collections.singleton(Component.class));
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
            BootstrapWith.class
        ));
    }

    @Override
    public boolean addClass(final Class<?> stepClass) {
        if (!stepClasses.contains(stepClass)) {
            checkNoComponentAnnotations(stepClass);
            if (dependsOnSpringContext(stepClass)) {
                if (stepClassWithSpringContext != null) {
                    throw new CucumberBackendException(String.format("" +
                        "Glue class %1$s and %2$s both attempt to configure the spring context. Please ensure only one " +
                        "glue class configures the spring context", stepClass, stepClassWithSpringContext));
                }
                stepClassWithSpringContext = stepClass;
            }
            stepClasses.add(stepClass);
        }
        return true;
    }

    @Override
    public void start() {
        if (stepClassWithSpringContext != null) {
            // The application context created by the TestContextManager is
            // a singleton and reused between scenarios and shared between
            // threads.
            TestContextManager testContextManager = new TestContextManager(stepClassWithSpringContext);
            testContextAdaptor = createTestContextManagerAdaptor(testContextManager, stepClasses);
        } else if (testContextAdaptor == null) {
            // The fallback application context is not shared between threads
            // (because the spring factory is not shared) but is reused
            // between scenarios
            if (getClass().getClassLoader().getResource(CUCUMBER_XML) == null) {
                ConfigurableApplicationContext applicationContext = new GenericApplicationContext();
                testContextAdaptor = createApplicationContextAdaptor(applicationContext, stepClasses);
            } else {
                // Application context is refreshed by TestContextManager.start
                // can't be done twice
                boolean refresh = false;
                String[] configLocations = {CUCUMBER_XML};
                ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(configLocations, refresh);
                testContextAdaptor = createApplicationContextAdaptor(applicationContext, stepClasses);
            }
        }
        testContextAdaptor.start();
        GlueCodeContext.getInstance().start();
    }

    @Override
    public void stop() {
        testContextAdaptor.stop();
        GlueCodeContext.getInstance().stop();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        try {
            return testContextAdaptor.getInstance(type);
        } catch (BeansException e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

}