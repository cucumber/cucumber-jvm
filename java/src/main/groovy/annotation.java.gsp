package io.cucumber.java.${lang};

import cucumber.runtime.java.StepDefAnnotation;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
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
@API(status = API.Status.STABLE)
public @interface ${kw} {
    /**
     * A cucumber or regular expression.
     *
     * @return a cucumber or regular expression
     */
    String value();

    /**
     * Duration in milliseconds this step is allowed to run. Cucumber
     * will mark the step as failed when exceeded.
     *
     * When the maximum  duration is exceeded the thread will
     * receive an in interrupt. Note: if the interrupt is ignored
     * cucumber will wait for the this hook to finish.
     *
     * @return timeout in milliseconds. 0 (default) means no restriction.
     */
    long timeout() default 0;
}
