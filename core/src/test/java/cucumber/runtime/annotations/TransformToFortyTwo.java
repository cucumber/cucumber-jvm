package cucumber.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cucumber.api.Transform;
import cucumber.runtime.ParameterInfoTest.FortyTwoTransformer;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
@Transform(FortyTwoTransformer.class)
public @interface TransformToFortyTwo {

}
