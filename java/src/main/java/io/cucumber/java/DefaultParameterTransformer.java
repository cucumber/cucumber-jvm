package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows a default transformer for all parameters to be registered.
 * <p>
 * Supports ParameterByTypeTransformer: String,Type -> T
 * Supports ParameterByTypeTransformer: Object,Type -> T
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface DefaultParameterTransformer {

}
