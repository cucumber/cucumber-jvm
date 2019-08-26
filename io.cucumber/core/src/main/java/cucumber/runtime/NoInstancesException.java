package cucumber.runtime;

public class NoInstancesException extends CucumberException {

    public NoInstancesException(Class parentType) {
        super(createMessage(parentType));
    }

    private static String createMessage(Class parentType) {
        return String.format("Couldn't find a single implementation of " + parentType);
    }
}
