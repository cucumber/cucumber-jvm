package cucumber.annotation.en;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface But {
    /**
     * @return a regular expression
     */
    String value();

    /**
     * @return a tag expression
     */
    String[] tags() default {};
}

