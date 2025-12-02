package io.cucumber.core.backend;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

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

    /**
     * @deprecated use {@link #getCause()} instead.
     */
    @Deprecated
    public @Nullable Throwable getInvocationTargetExceptionCause() {
        return getCause();
    }

    public Located getLocated() {
        return located;
    }

    @Override
    @SuppressWarnings("UnsynchronizedOverridesSynchronized")
    public @Nullable Throwable getCause() {
        return invocationTargetException.getCause();
    }
}
