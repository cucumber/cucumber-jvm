package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register doc string type.
 * <p>
 * The method must have this signature:
 * <ul>
 * <li>{@code String -> Author}</li>
 * </ul>
 * NOTE: {@code Author} is an example of the type of the parameter type.
 * <p>
 * Each docstring has a content type (text, json, ect) and type. The When not
 * provided in the annotation the content type is the name of the annotated
 * method. The type is the return type of the annotated. method.
 * <p>
 * Cucumber selects the doc string type to convert a docstring to the target
 * used in a step definition by:
 * <ol>
 * <li>Searching for an exact match of content type and target type</li>
 * <li>Searching for a unique match for target type</li>
 * <li>Throw an exception of zero or more then one docstring type was found</li>
 * </ol>
 * By default, Cucumber registers a docstring type for the anonymous content
 * type (i.e. no content type) and type {@link java.lang.String}.
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
