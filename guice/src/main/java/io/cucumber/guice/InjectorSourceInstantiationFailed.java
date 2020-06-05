package io.cucumber.guice;

import io.cucumber.core.backend.CucumberBackendException;

class InjectorSourceInstantiationFailed extends CucumberBackendException {

    InjectorSourceInstantiationFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
