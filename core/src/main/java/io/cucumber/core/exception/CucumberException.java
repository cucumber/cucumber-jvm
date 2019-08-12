package io.cucumber.core.exception;

public class CucumberException extends RuntimeException {

    public CucumberException(String message) {
        super(message);
    }

    public CucumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public CucumberException(Throwable cause) {
        super(cause);
    }

}
