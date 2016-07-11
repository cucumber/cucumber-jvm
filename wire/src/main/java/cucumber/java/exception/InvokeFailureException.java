package cucumber.java.exception;

public class InvokeFailureException extends CukeException {
    public InvokeFailureException(String message) {
        super(message);
    }

    public InvokeFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
