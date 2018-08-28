package cucumber.runtime;

import java.util.Collection;

public class TooManyInstancesException extends CucumberException {

    TooManyInstancesException(Collection instances) {
        super(createMessage(instances));
    }

    private static String createMessage(Collection instances) {
        return String.format("Expected only one instance, but found too many: " + instances);
    }
}
