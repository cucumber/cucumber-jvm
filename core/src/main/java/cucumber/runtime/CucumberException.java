package cucumber.runtime;

public class CucumberException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1393513206771603671L;

    public CucumberException(String message) {
        super(message);
    }

    public CucumberException(String message, Throwable e) {
        super(message, e);
    }
}
