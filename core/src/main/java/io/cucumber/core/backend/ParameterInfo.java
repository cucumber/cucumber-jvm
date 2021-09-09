package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.lang.reflect.Type;

@API(status = API.Status.STABLE)
public interface ParameterInfo {

    /**
     * Returns the type of this parameter. This type is used to provide a hint
     * to cucumber expressions to resolve anonymous parameter types.
     * <p>
     * Should always return the same value as {@link TypeResolver#resolve()} but
     * may not throw any exceptions. May return {@code Object.class} when no
     * information is available.
     *
     * @return the type of this parameter.
     */
    Type getType();

    /**
     * True if the data table should be transposed.
     *
     * @return true iff the data table should be transposed.
     */
    boolean isTransposed();

    /**
     * Returns a type resolver. The type provided by this resolver is used to
     * convert data table and doc string arguments to a java object.
     *
     * @return a type resolver
     */
    TypeResolver getTypeResolver();

}
