package cucumber.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be specified on hooks (methods annotated with {@link Before} and {@link After}) to specify 
 * the order in which the hooks should run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Order {
    int value();
}
