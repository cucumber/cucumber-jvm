package cucumber.java.exception;

public class InvokeException extends CukeException {

    public InvokeException(String message) {
        super(message);
    }

    public InvokeException(String message, Exception cause) {
        super(message, cause);
    }
}
