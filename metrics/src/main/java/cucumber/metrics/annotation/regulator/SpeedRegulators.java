package cucumber.metrics.annotation.regulator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpeedRegulators {

    /**
     * @return Array used for multiple SpeedRegulator declarations.
     */
    SpeedRegulator[] value();

}