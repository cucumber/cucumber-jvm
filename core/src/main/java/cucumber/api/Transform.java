package cucumber.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to specify how a Step Definition argument is transformed.
 *
 * @see Transformer
 */
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@java.lang.annotation.Documented
public @interface Transform {
    Class<? extends Transformer> value();
}
