package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Executes a method after all scenarios
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.EXPERIMENTAL)
public @interface AfterAll {

    /**
     * The order in which this hook should run. Higher numbers are run first.
     * The default order is 10000.
     *
     * @return the order in which this hook should run.
     */
    int order() default 10000;
}
