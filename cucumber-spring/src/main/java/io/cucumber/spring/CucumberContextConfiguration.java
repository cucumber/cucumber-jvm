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
 * <pre>{@code
 * &#064;CucumberContextConfiguration
 * &#064;SpringBootTest(classes = TestConfig.class)
 * public class CucumberSpringConfiguration {
 * <p>
 * }
 * }</pre>
 *
 * Notes:
 * <ul>
 * <li>Only one glue class should be annotated with
 * {@code @CucumberContextConfiguration} otherwise an exception will be
 * thrown.</li>
 * <li>Cucumber Spring uses Spring's {@code TestContextManager} framework
 * internally. As a result a single Cucumber scenario will mostly behave like a
 * JUnit test.</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.STABLE)
public @interface CucumberContextConfiguration {

}
