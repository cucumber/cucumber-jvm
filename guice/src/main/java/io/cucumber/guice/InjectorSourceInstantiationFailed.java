package io.cucumber.guice;

import io.cucumber.core.exception.CucumberException;

class InjectorSourceInstantiationFailed extends CucumberException {

    InjectorSourceInstantiationFailed(String message, Throwable cause) {
        super(message, cause);
    }
}