package io.cucumber.core.exception;

import org.jspecify.annotations.Nullable;

public class CucumberException extends RuntimeException {

    public CucumberException(@Nullable String message) {
        super(message);
    }

    public CucumberException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public CucumberException(@Nullable Throwable cause) {
        super(cause);
    }

}
