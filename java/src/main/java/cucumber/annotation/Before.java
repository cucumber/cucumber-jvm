package cucumber.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Before {
    /**
     * @return a tag expression
     */
    String[] value() default {};

    /**
     * @return max amount of time this is allowed to run for. 0 (default) means no restriction.
     */
    int timeout() default 0;
}
