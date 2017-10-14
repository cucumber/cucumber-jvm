package cucumber.java.connectors.wire;

public class PendingResponse implements WireResponse {
    private String message;

    public PendingResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
}
