package cucumber.api;

import cucumber.runtime.xstream.converters.SingleValueConverter;

import java.lang.annotation.ElementType;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.PARAMETER})
@java.lang.annotation.Documented
/**
 * Step definition parameters can be annotated with this annotation to give full
 * control over how a step argument gets converted to an object. Although the
 * annotation value can be a {@link SingleValueConverter} it is recommended to
 * use a {@link Transformer} subclass instead.
 *
 * @see Transformer
 */
public @interface Transform {
    Class<? extends SingleValueConverter> value();
}
