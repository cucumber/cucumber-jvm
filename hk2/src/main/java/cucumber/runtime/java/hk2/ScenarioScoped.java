package cucumber.runtime.java.hk2;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Cucumber Scenario scope binding annotation. Limit the scope of an object to the lifecycle of a Scenario.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Scope
public @interface ScenarioScoped {
}
