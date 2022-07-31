package io.cucumber.core.gherkin;

public final class FeatureParserException extends RuntimeException {

    public FeatureParserException(String message) {
        super(message);
    }

    public FeatureParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeatureParserException(Throwable cause) {
        super(cause);
    }

}
