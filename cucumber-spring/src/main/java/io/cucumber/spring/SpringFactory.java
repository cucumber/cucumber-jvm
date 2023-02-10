package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.resource.ClasspathSupport;
import org.apiguardian.api.API;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;
import java.util.HashSet;

import static io.cucumber.spring.TestContextAdaptor.create;

/**
 * Spring based implementation of ObjectFactory.
 * <p>
 * Application beans are accessible from the step definitions using autowiring
 * (with annotations).
 * <p>
 * The spring context can be configured by annotating one glue class with
 * a @{@link CucumberContextConfiguration} and any one of the
 * following @{@link ContextConfiguration}, @{@link ContextHierarchy}
 * or @{@link BootstrapWith}. This glue class can also be annotated
 * with @{@link WebAppConfiguration} or @{@link DirtiesContext} annotation.
 * <p>
 * Notes:
 * <ul>
 * <li>SpringFactory uses Springs TestContextManager framework to manage the
 * spring context. The class annotated with {@code CucumberContextConfiguration}
 * will be use to instantiate the {@link TestContextManager}.</li>
 * <li>If not exactly one glue class is annotated with
 * {@code CucumberContextConfiguration} an exception will be thrown.</li>
 * <li>Step definitions should not be annotated with @{@link Component} or other
 * annotations that mark it as eligible for detection by classpath scanning.
 * When a step definition class is annotated by @Component or an annotation that
 * has the @Component stereotype an exception will be thrown</li>
 * </ul>
 */
@API(status = API.Status.STABLE)
public final class SpringFactory implements ObjectFactory {

    private final Collection<Class<?>> stepClasses = new HashSet<>();
    private Class<?> withCucumberContextConfiguration = null;
    private TestContextAdaptor testContextAdaptor;

    @Override
    public boolean addClass(final Class<?> stepClass) {
        if (stepClasses.contains(stepClass)) {
            return true;
        }

        checkNoComponentAnnotations(stepClass);
        if (hasCucumberContextConfiguration(stepClass)) {
            checkOnlyOneClassHasCucumberContextConfiguration(stepClass);
            withCucumberContextConfiguration = stepClass;
        }
        stepClasses.add(stepClass);
        return true;
    }

    private static void checkNoComponentAnnotations(Class<?> type) {
        if (AnnotatedElementUtils.isAnnotated(type, Component.class)) {
            throw new CucumberBackendException(String.format("" +
                    "Glue class %1$s was (meta-)annotated with @Component; marking it as a candidate for auto-detection by "
                    +
                    "Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by "
                    +
                    "spring may lead to duplicate bean definitions. Please remove the @Component (meta-)annotation",
                type.getName()));
        }
    }

    static boolean hasCucumberContextConfiguration(Class<?> stepClass) {
        return AnnotatedElementUtils.isAnnotated(stepClass, CucumberContextConfiguration.class);
    }

    private void checkOnlyOneClassHasCucumberContextConfiguration(Class<?> stepClass) {
        if (withCucumberContextConfiguration != null) {
            throw new CucumberBackendException(String.format("" +
                    "Glue class %1$s and %2$s are both (meta-)annotated with @CucumberContextConfiguration.\n" +
                    "Please ensure only one class configures the spring context\n" +
                    "\n" +
                    "By default Cucumber scans the entire classpath for context configuration.\n" +
                    "You can restrict this by configuring the glue path.\n" +
                    ClasspathSupport.configurationExamples(),
                stepClass,
                withCucumberContextConfiguration));
        }
    }

    @Override
    public void start() {
        if (withCucumberContextConfiguration == null) {
            throw new CucumberBackendException("" +
                    "Please annotate a glue class with some context configuration.\n" +
                    "\n" +
                    "For example:\n" +
                    "\n" +
                    "   @CucumberContextConfiguration\n" +
                    "   @SpringBootTest(classes = TestConfig.class)\n" +
                    "   public class CucumberSpringConfiguration { }" +
                    "\n" +
                    "Or: \n" +
                    "\n" +
                    "   @CucumberContextConfiguration\n" +
                    "   @ContextConfiguration( ... )\n" +
                    "   public class CucumberSpringConfiguration { }");
        }

        // The application context created by the TestContextManager is
        // a singleton and reused between scenarios and shared between
        // threads.
        testContextAdaptor = create(() -> new TestContextManager(withCucumberContextConfiguration), stepClasses);
        testContextAdaptor.start();
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
