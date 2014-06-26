package cucumber.runtime.java.guice.impl;

public class InjectorSourceInstantiationFailed extends RuntimeException {

    public InjectorSourceInstantiationFailed(String message, Throwable cause) {
        super(message, cause);
    }
}