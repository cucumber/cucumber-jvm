package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Execute method before each scenario.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface Before {

    /**
     * Tag expression. If the expression applies to the current scenario this
     * hook will be executed.
     *
     * @return a tag expression
     */
    String value() default "";

    /**
     * @return the order in which this hook should run. Lower numbers are run
     *         first. The default order is 10000.
     */
    int order() default 10000;

}
