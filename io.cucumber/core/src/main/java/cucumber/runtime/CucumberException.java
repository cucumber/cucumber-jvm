package cucumber.runtime;

public class CucumberException extends RuntimeException {
    public CucumberException(String message) {
        super(message);
    }

    public CucumberException(String message, Throwable e) {
        super(message, e);
    }

    public CucumberException(Throwable e) {
        super(e);
    }
}
