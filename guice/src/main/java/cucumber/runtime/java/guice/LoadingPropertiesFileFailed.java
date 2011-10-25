package cucumber.runtime.java.guice;

public class LoadingPropertiesFileFailed extends RuntimeException {

    public LoadingPropertiesFileFailed(String message, Throwable cause) {
        super(message, cause);
    }
}