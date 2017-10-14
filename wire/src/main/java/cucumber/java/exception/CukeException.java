package cucumber.java.exception;

public class CukeException extends Exception {
    public CukeException(String message) {
        super(message);
    }

    public CukeException(String message, Throwable cause) {
        super(message, cause);
    }
}
