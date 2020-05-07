package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register default parameter type transformer.
 * <p>
 * Valid method signatures are:
 * <ul>
 * <li>{@code String, Type -> Object}</li>
 * <li>{@code Object, Type -> Object}</li>
 * </ul>
 *
 * @see io.cucumber.cucumberexpressions.ParameterByTypeTransformer
 * @see io.cucumber.cucumberexpressions.ParameterType
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface DefaultParameterTransformer {

}
