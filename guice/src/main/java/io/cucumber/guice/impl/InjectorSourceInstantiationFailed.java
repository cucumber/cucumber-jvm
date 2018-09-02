package io.cucumber.guice.impl;

import io.cucumber.core.exception.CucumberException;

public class InjectorSourceInstantiationFailed extends CucumberException {

    public InjectorSourceInstantiationFailed(String message, Throwable cause) {
        super(message, cause);
    }
}