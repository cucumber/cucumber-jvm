package cucumber.classpath;

import cucumber.runtime.CucumberException;

public class NoSuchResourceException extends CucumberException {
    public NoSuchResourceException(String message) {
        super(message);
    }
}
