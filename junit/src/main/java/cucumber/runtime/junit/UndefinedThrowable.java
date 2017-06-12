package cucumber.runtime.junit;


public class UndefinedThrowable extends Throwable {
    private static final long serialVersionUID = 1L;

    public UndefinedThrowable() {
        super("This step is undefined", null, false, false);
    }

    public UndefinedThrowable(String stepText) {
        super(String.format("The step \"%s\" is undefined", stepText), null, false, false);
    }
}
