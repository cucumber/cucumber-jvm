package cucumber.api.osgi;

public class ServiceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServiceNotFoundException() {
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
