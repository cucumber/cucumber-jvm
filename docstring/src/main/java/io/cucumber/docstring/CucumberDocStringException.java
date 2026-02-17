package io.cucumber.docstring;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE)
public final class CucumberDocStringException extends RuntimeException {

    CucumberDocStringException(@Nullable String message) {
        super(message);
    }

    CucumberDocStringException(@Nullable String message, Throwable throwable) {
        super(message, throwable);
    }

}
