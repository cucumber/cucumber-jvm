package io.cucumber.junit.platform.engine;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test discovery annotation. Marks the package of the annotated class for test
 * discovery.
 * <p>
 * Maven and Gradle do not support the
 * {@link org.junit.platform.engine.discovery.DiscoverySelectors} used by
 * Cucumber. As a workaround Cucumber will scan the package of the annotated
 * class for feature files and execute them.
 * <p>
 * Note about Testable: While this class is annotated with @Testable the
 * recommended way for IDEs and other tooling use the selectors implemented by
 * Cucumber to discover feature files.
 * <p>
 * 
 * @deprecated Please use the JUnit Platform Suite to run Cucumber in
 *             combination with Surefire or Gradle. E.g:
 * 
 *             <pre>{@code
 *package com.example;
 *
 *import org.junit.platform.suite.api.ConfigurationParameter;
 *import org.junit.platform.suite.api.SelectPackages;
 *import org.junit.platform.suite.api.Suite;
 *
 *import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
 *
 *             @Suite
 *             @SelectPackages("com.example")
 *             @ConfigurationParameter(
 *             key = GLUE_PROPERTY_NAME,
 *             value = "com.example")
 *             public class RunCucumberTest {
 *             }
 * }</pre>
 * 
 * @see        CucumberTestEngine
 */
@API(status = Status.DEPRECATED)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Testable
@Deprecated
public @interface Cucumber {

}
