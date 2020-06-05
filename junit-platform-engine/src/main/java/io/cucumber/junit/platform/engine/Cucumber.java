package io.cucumber.junit.platform.engine;

import org.apiguardian.api.API;
import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test discovery annotation. Marks the package of the annotated class for test
 * discovery.
 * <p>
 * Some build tools do not support the
 * {@link org.junit.platform.engine.discovery.DiscoverySelectors} used by
 * Cucumber. As a work around Cucumber will scan the package of the annotated
 * class for feature files and execute them.
 * <p>
 * Note about Testable: While this class is annotated with @Testable the
 * recommended way for IDEs and other tooling use the selectors implemented by
 * Cucumber to discover feature files.
 *
 * @see CucumberTestEngine
 */
@API(status = API.Status.STABLE)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Testable
public @interface Cucumber {

}
