package cucumber.metrics.annotation.regulator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpeedRegulator {

    /**
     * @return the key of targeted application.
     */
    String application() default "";

    /**
     * @return number of interaction with the targeted application server. -1 (default) means no restriction.
     */
    int cost() default -1;

    /**
     * @return number of interaction with the targeted application server (from properties). "" (default) not take account of this parameter.
     */
    String costString() default "";

    /**
     * @return Time units of delay.
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * @return Shall it be fully verbose (show full exception trace) or just
     */
    boolean verbose() default false;

}