package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.lang.reflect.InvocationTargetException;

import static java.util.Objects.requireNonNull;

/**
 * Thrown when an exception was thrown by glue code. Not to be confused with
 * {@link CucumberBackendException} which is thrown when the backend failed to
 * invoke the glue.
 */
@API(status = API.Status.STABLE)
public final class CucumberInvocationTargetException extends RuntimeException {

    private final Located located;
    private final Throwable cause;

    public CucumberInvocationTargetException(Located located, InvocationTargetException invocationTargetException) {
        super(invocationTargetException.getCause());
        this.located = located;
        this.cause = requireNonNull(invocationTargetException.getCause());
    }

    /**
     * @deprecated use {@link #getCause()} instead.
     */
    @Deprecated
    public Throwable getInvocationTargetExceptionCause() {
        return getCause();
    }

    public Located getLocated() {
        return located;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }
}
