package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.CucumberAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be specified on Hooks (methods annotated with @Before and @After).
 * It can be used to specify the order in which Hooks should run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CucumberAnnotation("en")
public @interface Order {
    int value();
}