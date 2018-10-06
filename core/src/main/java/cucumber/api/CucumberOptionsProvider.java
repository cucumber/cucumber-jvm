package cucumber.api;

import cucumber.runtime.AbstractCucumberOptionsProvider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a class extending {@link AbstractCucumberOptionsProvider} which will provide additional
 * Cucumber options during runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CucumberOptionsProvider {
  /** @return the class used to contribute options */
  Class<? extends AbstractCucumberOptionsProvider> value() default
      AbstractCucumberOptionsProvider.class;
}
