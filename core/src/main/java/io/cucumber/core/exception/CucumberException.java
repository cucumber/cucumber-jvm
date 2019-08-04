package io.cucumber.core.exception;

public class CucumberException extends RuntimeException {

    public CucumberException(final String message) {
        super(message);
    }

    public CucumberException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CucumberException(final Throwable cause) {
        super(cause);
    }

}
