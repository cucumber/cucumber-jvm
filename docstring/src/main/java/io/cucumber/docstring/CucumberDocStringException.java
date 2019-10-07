package io.cucumber.docstring;

final class CucumberDocStringException extends RuntimeException {

    CucumberDocStringException(String message) {
        super(message);
    }

    CucumberDocStringException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
