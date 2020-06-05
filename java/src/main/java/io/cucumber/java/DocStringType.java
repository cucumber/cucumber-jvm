package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register doc string type.
 * <p>
 * The name of the method is used as the content type of the
 * {@link io.cucumber.docstring.DocStringType}.
 * <p>
 * The method must have this signature:
 * <ul>
 * <li>{@code String -> Author}</li>
 * </ul>
 * NOTE: {@code Author} is an example of the type of the parameter type.
 *
 * @see io.cucumber.docstring.DocStringType
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface DocStringType {

    /**
     * Name of the content type.
     * <p>
     * When not provided this will default to the name of the annotated method.
     *
     * @return content type
     */
    String contentType() default "";

}
