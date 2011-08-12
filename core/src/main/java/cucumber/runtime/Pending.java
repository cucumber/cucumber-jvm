package cucumber.runtime;

public class Pending extends RuntimeException {
    public Pending() {
        super();
    }

    public Pending(String message) {
        super(message);
    }
}
