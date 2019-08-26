package cucumber.api.osgi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface Filter {

    String value() default "";

    long timeout() default 0;
}
