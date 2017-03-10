package cucumber.metrics.annotation.time;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Times {

    /**
     * @return Array used for multiple SpeedRegulator declarations.
     */
    Time[] value();

}
