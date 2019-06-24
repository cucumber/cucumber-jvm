package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface After {
    /**
     * Tag expression. If the expression applies to the current
     * scenario this hook will be executed.
     *
     * @return a tag expression
     */
    String value() default "";

    /**
     * Duration in milliseconds this hook is allowed to run. Cucumber
     * will mark the hook as failed when exceeded.
     *
     * When the maximum duration is exceeded the thread will
     * receive an interrupt. Note: if the interrupt is ignored
     * Cucumber will wait for the this hook to finish.
     *
     * @return timeout in milliseconds. 0 (default) means no restriction.
     */
    long timeout() default 0;

    /**
     * @return the order in which this hook should run. Higher numbers are run first.
     * The default order is 10000.
     */
    int order() default 10000;
}
