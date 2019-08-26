package cucumber.api;

// We're deliberately not extending CucumberException (which is used to signal fatal errors)
@Pending
public class PendingException extends RuntimeException {
    public PendingException() {
        this("TODO: implement me");
    }

    public PendingException(String message) {
        super(message);
    }
}
