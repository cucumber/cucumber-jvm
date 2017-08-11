package cucumber.metrics.annotation.time;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Time {

    /**
     * @return The name of the timer.
     */
    String name() default "";

    /**
     * @return value of increment.
     */
    int mark() default 1;

    /**
     * @return Shall it be fully verbose (show full exception trace) or just
     */
    boolean verbose() default false;

    /**
     * @return Shall it be fully verbose (show full exception trace) or just
     */
    boolean jmx() default false;

}