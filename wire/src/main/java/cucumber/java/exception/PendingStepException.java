package cucumber.java.exception;

public class PendingStepException extends CukeException {
    public PendingStepException(String message) {
        super(message);
    }

    public PendingStepException(String message, Throwable cause) {
        super(message, cause);
    }
}
