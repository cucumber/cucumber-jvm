package io.cucumber.spring;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on a configuration class to make the Cucumber aware
 * of the test configuration. This is to be used in conjunction with
 * {@code @ContextConfiguration}, {@code @ContextHierarchy} or
 * {@code @BootstrapWith}. In case of SpringBoot, the configuration class can be
 * annotated as follows:
 * <p>
 * 
 * <pre>
 * &#64;CucumberContextConfiguration
 * &#64;SpringBootTest(classes = TestConfig.class)
 * public class CucumberSpringConfiguration {
 * }
 * </pre>
 * <p>
 * Notes:
 * <ul>
 * <li>Only one glue class should be annotated with
 * {@code @CucumberContextConfiguration} otherwise an exception will be
 * thrown.</li>
 * <li>Cucumber Spring uses Spring's {@code TestContextManager} framework
 * internally. As a result a single Cucumber scenario will mostly behave like a
 * JUnit test.</li>
 * <li>The class annotated with {@code CucumberContextConfiguration} is
 * instantiated but not initialized by Spring. This instance is processed by
 * Springs {@link org.springframework.test.context.TestExecutionListener
 * TestExecutionListeners}. So features that depend on a test execution listener
 * such as mock beans will work on the annotated class - but not on other step
 * definition classes. Features that depend on initializing beans - such as
 * AspectJ - will not work on the annotated class - but will work on step
 * definition classes.</li>
 * <li></li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.STABLE)
public @interface CucumberContextConfiguration {

}
