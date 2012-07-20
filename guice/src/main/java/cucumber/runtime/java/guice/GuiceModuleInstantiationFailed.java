package cucumber.runtime.java.guice;

public class GuiceModuleInstantiationFailed extends RuntimeException {

    public GuiceModuleInstantiationFailed(String message, Throwable cause) {
        super(message, cause);
    }
}