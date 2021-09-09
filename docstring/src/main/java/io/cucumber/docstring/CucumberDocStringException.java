package io.cucumber.docstring;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class CucumberDocStringException extends RuntimeException {

    CucumberDocStringException(String message) {
        super(message);
    }

    CucumberDocStringException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
