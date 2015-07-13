package cucumber.runtime.java.guice.impl;

import cucumber.runtime.CucumberException;

public class InjectorSourceInstantiationFailed extends CucumberException {

    public InjectorSourceInstantiationFailed(String message, Throwable cause) {
        super(message, cause);
    }
}