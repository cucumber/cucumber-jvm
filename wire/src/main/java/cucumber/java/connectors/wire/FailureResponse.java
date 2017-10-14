package cucumber.java.connectors.wire;

public class FailureResponse implements WireResponse {
    private String message;
    private String exception;

    public FailureResponse() {
    }

    public FailureResponse(Exception exception) {
        this.exception = exception.getClass().getCanonicalName();
    }

    public FailureResponse(String message, Exception exception) {
        this.message = message;
        this.exception = exception.getClass().getCanonicalName();
    }

    public String getMessage() { return message; }
    public String getException() { return exception; }
}
