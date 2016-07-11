package cucumber.java.exception;

public class CukeRuntimeException extends CukeException {
    public CukeRuntimeException(String message) {
        super(message);
    }

    public CukeRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
