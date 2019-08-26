package cucumber.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any exception class annotated with this annotation will be treated as a "pending" exception.
 * That is - if the exception is thrown from a step definition or hook, the scenario's status will
 * be pending instead of failed.
 *
 * @see PendingException
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Pending {
}
