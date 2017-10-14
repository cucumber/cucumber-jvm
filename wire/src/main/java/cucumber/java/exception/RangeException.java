package cucumber.java.exception;

public class RangeException extends CukeException {
    public RangeException(String message) {
        super(message);
    }

    public RangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
