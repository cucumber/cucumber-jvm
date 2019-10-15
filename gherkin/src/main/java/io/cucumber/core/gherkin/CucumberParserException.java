package io.cucumber.core.gherkin;

public final class CucumberParserException extends RuntimeException {

    public CucumberParserException(String message) {
        super(message);
    }

    public CucumberParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CucumberParserException(Throwable cause) {
        super(cause);
    }
}
