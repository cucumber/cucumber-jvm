package io.cucumber.core.exception;

public class UnrecoverableExceptions {
    public static void rethrowIfUnrecoverable(Throwable exception) {
        if (exception instanceof OutOfMemoryError) {
            ExceptionUtils.throwAsUncheckedException(exception);
        }
    }
}
