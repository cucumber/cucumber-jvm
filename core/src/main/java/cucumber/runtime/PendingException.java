package cucumber.runtime;

public class PendingException extends RuntimeException {
    public PendingException(String message) {
        super(message);
    }
}
