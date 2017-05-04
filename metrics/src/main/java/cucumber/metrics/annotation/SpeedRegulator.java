package cucumber.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

}