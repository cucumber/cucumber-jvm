package io.cucumber.core.stepexpression;

import org.apiguardian.api.API;

import java.lang.reflect.Type;

/**
 * Allows lazy resolution of the type of a data table or doc string.
 */
@API(status = API.Status.STABLE)
public interface TypeResolver {

    /**
     * A type to data convert the table or doc string to. May not return null.
     * <p>
     * When the {@link Object} type is returned no transform will be applied.
     *
     * @return a type
     */
    Type resolve();

}
