package io.cucumber.java.api.annotation.${lang};

import io.cucumber.java.StepDefAnnotation;
import io.cucumber.java.StepDefAnnotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To execute steps in a feature file the steps must be
 * connected to executable code. This can be done by annotating
 * a method with a cucumber or regular expression.
 * <p>
 * The parameters extracted from the step by the expression
 * along with the data table or doc string argument are provided as
 * arguments to the method.
 * <p>
 * The types of the parameters are determined by the cucumber or
 * regular expression.
 * <p>
 * The type of the data table or doc string argument is determined
 * by the argument name value. When none is provided cucumber will
 * attempt to transform the data table or doc string to the type
 * of the last argument.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@StepDefAnnotation
@Documented
@Repeatable(${kw}.${kw}s.class)
public @interface ${kw} {
    /**
     * @return a cucumber or regular expression
     */
    String value();

    /**
     * @return max amount of milliseconds this is allowed to run for. 0 (default) means no restriction.
     */
    long timeout() default 0;

    /**
     * Allows the use of multiple '${kw}'s on a single method.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @StepDefAnnotations
    @Documented
    @interface ${kw}s {
        ${kw}[] value();
    }
}

