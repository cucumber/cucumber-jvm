package cucumber.java.step;

public class InvokeResult {
    private InvokeResultType type;
    private String description;

    private InvokeResult(InvokeResultType type, String description) {
        this.type = type;
        this.description = description;
    }

    public InvokeResult() {
        type = InvokeResultType.FAILURE;
    }

    public static InvokeResult success() {
        return new InvokeResult(InvokeResultType.SUCCESS, null);
    }

    public static InvokeResult failure(String description) {
        return new InvokeResult(InvokeResultType.FAILURE, description);
    }

    public static InvokeResult pending(String description) {
        return new InvokeResult(InvokeResultType.PENDING, description);
    }

    public boolean isSuccess() {
        return (type == InvokeResultType.SUCCESS);
    }

    public boolean isPending() {
        return (type == InvokeResultType.PENDING);
    }

    public InvokeResultType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
