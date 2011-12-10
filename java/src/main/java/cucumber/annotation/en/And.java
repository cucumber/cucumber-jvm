package cucumber.annotation.en;

import cucumber.runtime.java.StepDefAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@StepDefAnnotation
public @interface And {
    /**
     * @return a regular expression
     */
    String value();
}

