package cucumber.api;

import cucumber.runtime.xstream.converters.SingleValueConverter;

import java.lang.annotation.ElementType;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.PARAMETER})
@java.lang.annotation.Documented
public @interface Transform {
    Class<? extends SingleValueConverter> value();
}
