package io.cucumber.core.backend;

import org.apiguardian.api.API;

/**
 * Thrown when the backend could not invoke some glue code. Not to be confused
 * with {@link CucumberInvocationTargetException} which is thrown when the glue
 * code throws an exception.
 */
@API(status = API.Status.STABLE)
public class CucumberBackendException extends RuntimeException {

    public CucumberBackendException(String message) {
        super(message);
    }

    public CucumberBackendException(String message, Throwable cause) {
        super(message, cause);
    }

}
