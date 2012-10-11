package cucumber.api;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.PARAMETER})
@java.lang.annotation.Documented
public @interface Transform {
    Class<? extends SingleValueConverter> value();
}
