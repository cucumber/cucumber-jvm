package io.cucumber.core.backend;

public class CucumberBackendException extends RuntimeException {

    public CucumberBackendException(String message) {
        super(message);
    }

    public CucumberBackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
