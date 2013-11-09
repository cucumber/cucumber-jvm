package cucumber.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import cucumber.api.Transform;
import cucumber.runtime.ParameterInfoTest.FortyTwoTransformer;

@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.PARAMETER})
@java.lang.annotation.Documented
@Transform(FortyTwoTransformer.class)
public @interface TransformToFortyTwo {

}
