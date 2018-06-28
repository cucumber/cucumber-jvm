package cucumber.api.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterStep {
    /**
     * @return a tag expression
     */
    String[] value() default {};

    /**
     * @return max amount of milliseconds this is allowed to run for. 0 (default) means no restriction.
     */
    long timeout() default 0;

    /**
     * @return the order in which this hook should run. Higher numbers are run first.
     * The default order is 10000.
     */
    int order() default 10000;
}
