package cucumber.api.android;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO (aslak): In stead of a stripped down copy of cucumber-junit's @Cucumber.Options annotation
// it would be better to pull that annotation up to core and deprecate the JUnit one.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RunWithCucumber {
    String glue() default "";

    String features() default "features";
}
