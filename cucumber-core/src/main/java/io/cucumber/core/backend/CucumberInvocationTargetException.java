package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.lang.reflect.InvocationTargetException;

/**
 * Thrown when an exception was thrown by glue code. Not to be confused with
 * {@link CucumberBackendException} which is thrown when the backend failed to
 * invoke the glue.
 */
@API(status = API.Status.STABLE)
public final class CucumberInvocationTargetException extends RuntimeException {

    private final Located located;
    private final InvocationTargetException invocationTargetException;

    public CucumberInvocationTargetException(Located located, InvocationTargetException invocationTargetException) {
        this.located = located;
        this.invocationTargetException = invocationTargetException;
    }

    public Throwable getInvocationTargetExceptionCause() {
        return invocationTargetException.getCause();
    }

    public Located getLocated() {
        return located;
    }

}
