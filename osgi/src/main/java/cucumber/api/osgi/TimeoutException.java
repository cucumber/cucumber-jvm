package cucumber.api.osgi;

public class TimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TimeoutException() {
    }

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(Throwable cause) {
        super(cause);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
