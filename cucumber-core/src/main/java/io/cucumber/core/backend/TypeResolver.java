package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.lang.reflect.Type;

/**
 * Allows lazy resolution and validation of the type of a data table or doc
 * string argument.
 */
@API(status = API.Status.STABLE)
public interface TypeResolver {

    /**
     * A type to convert the data table or doc string to.
     * <p>
     * May throw an exception when the type could not adequately be determined
     * for instance due to a lack of generic information. If a value is return
     * it must be the same as {@link ParameterInfo#getType()}
     * <p>
     * When the {@link Object} type is returned no transform will be applied to
     * the data table or doc string.
     *
     * @return                  a type
     * @throws RuntimeException when the type could not adequately be determined
     */
    Type resolve() throws RuntimeException;

}
