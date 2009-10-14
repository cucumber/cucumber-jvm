package billing;

/**
 * @author Henning Jensen
 */
public class CreateTransactionResponse {

    public enum Status {
        OK, ERROR;
    }

    private final Status status;
    private final String description;

    public CreateTransactionResponse(Status status, String description) {
        this.status = status;
        this.description = description;
    }

    public boolean isOK() {
        return Status.OK.equals(status);
    }

    public String getDescription() {
        return description;
    }
}
