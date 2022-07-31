package io.cucumber.core.exception;

/**
 * Utility for filtering out unrecoverable exceptions. Cucumber invokes methods
 * that may throw arbitrary exceptions. These can only be caught as
 * {@code Throwable}. Some of these such as {@link OutOfMemoryError} should
 * never be caught and end in termination of the application.
 */
public final class UnrecoverableExceptions {

    private UnrecoverableExceptions() {

    }

    public static void rethrowIfUnrecoverable(Throwable exception) {
        if (exception instanceof OutOfMemoryError) {
            ExceptionUtils.throwAsUncheckedException(exception);
        }
    }
}
