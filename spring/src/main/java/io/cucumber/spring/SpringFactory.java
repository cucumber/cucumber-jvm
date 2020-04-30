package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.apiguardian.api.API;
import org.springframework.beans.BeansException;
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

import static io.cucumber.spring.TestContextAdaptor.createClassPathXmlApplicationContextAdaptor;
import static io.cucumber.spring.TestContextAdaptor.createGenericApplicationContextAdaptor;
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
 * <li>Deprecated: If no step definition class with @ContextConfiguration or @ContextHierarchy
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
 * If more that one glue class is used to configure the spring context an exception will be thrown.
 * </li>
 * </ul>
 */
@API(status = API.Status.STABLE)
public final class SpringFactory implements ObjectFactory {

    private static final Logger log = LoggerFactory.getLogger(SpringFactory.class);

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

    @Deprecated
    private static boolean dependsOnSpringContext(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if (annotatedWithSupportedSpringRootTestAnnotations(annotation)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
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
            if (dependsOnSpringContext(stepClass) || hasCucumberContextConfiguration(stepClass)) {
                if (stepClassWithSpringContext != null) {
                    throw new CucumberBackendException(String.format("" +
                        "Glue class %1$s and %2$s both attempt to configure the spring context. Please ensure only one " +
                        "glue class configures the spring context", stepClass, stepClassWithSpringContext
                    ));
                }

                if (dependsOnSpringContext(stepClass) && !hasCucumberContextConfiguration(stepClass)) {
                    log.warn(() -> String.format(
                        "Glue class %1$s attempts to configure the spring context but was not annotated with %2$s.\n" +
                            "Implicit configuration of the spring context is deprecated.\n" +
                            "Please add the %2$s to %1$s", stepClass, CucumberContextConfiguration.class.getName()
                    ));
                }

                stepClassWithSpringContext = stepClass;
            }
            stepClasses.add(stepClass);
        }
        return true;
    }

    private boolean hasCucumberContextConfiguration(Class<?> stepClass) {
        return stepClass.getAnnotation(CucumberContextConfiguration.class) != null;
    }


    @Override
    public void start() {
        if (stepClassWithSpringContext != null) {
            // The application context created by the TestContextManager is
            // a singleton and reused between scenarios and shared between
            // threads.
            TestContextManager testContextManager = new TestContextManager(stepClassWithSpringContext);
            testContextAdaptor = createTestContextManagerAdaptor(testContextManager, stepClasses);
        } else if (getClass().getClassLoader().getResource(CUCUMBER_XML) == null) {
            warnAboutDeprecationOfGenericApplicationContext();
            // The generic fallback application context is not shared between
            // threads (because the spring factory is not shared) and not reused
            // between scenarios because we recreate it each time the spring
            // factory starts.
            testContextAdaptor = createGenericApplicationContextAdaptor(stepClasses);
        } else if (testContextAdaptor == null) {
            warnAboutDeprecationOfCucumberXml();

            // The xml fallback application context is not shared between
            // threads (because the spring factory is not shared) but is reused
            // between scenarios.
            String[] configLocations = {CUCUMBER_XML};
            testContextAdaptor = createClassPathXmlApplicationContextAdaptor(configLocations, stepClasses);
        }
        testContextAdaptor.start();
    }

    private void warnAboutDeprecationOfGenericApplicationContext() {
        log.warn(() -> "" +
            "Glue glue classes have been annotated with a Spring Context Configuration.\n" +
            "Falling back to a generic application context.\n" +
            "This fallback has beep deprecated. Please annotate a glue class with some context configuration.\n" +
            "\n" +
            "For example:\n" +
            "\n" +
            "   @@CucumberContextConfiguration\n" +
            "   @SpringBootTest(classes = TestConfig.class)\n" +
            "   public class CucumberSpringConfiguration { }" +
            "\n" +
            "Or: \n" +
            "\n" +
            "   @@CucumberContextConfiguration\n" +
            "   @ContextConfiguration( ... )\n" +
            "   public class CucumberSpringConfiguration { }"
        );
    }

    private void warnAboutDeprecationOfCucumberXml() {
        log.warn(() -> "" +
            "You are using cucumber.xml to configure the Spring Application Context.\n" +
            "cucumber.xml has been deprecated. Instead consider annotation based configuration.\n" +
            "You may create a glue class containing:\n" +
            "\n" +
            "   @@CucumberContextConfiguration\n" +
            "   @ContextConfiguration(\"classpath:cucumber.xml\")\n" +
            "   public class CucumberSpringConfiguration { }"
        );
    }


    @Override
    public void stop() {
        if (testContextAdaptor != null) {
            testContextAdaptor.stop();
        }
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