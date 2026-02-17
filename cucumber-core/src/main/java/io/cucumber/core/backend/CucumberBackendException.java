package io.cucumber.core.backend;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Thrown when the backend could not invoke some glue code. Not to be confused
 * with {@link CucumberInvocationTargetException} which is thrown when the glue
 * code throws an exception.
 */
@API(status = API.Status.STABLE)
public class CucumberBackendException extends RuntimeException {

    public CucumberBackendException(@Nullable String message) {
        super(message);
    }

    public CucumberBackendException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
