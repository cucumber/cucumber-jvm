package cucumber.runtime;

public class UndefinedStepDefinitionException extends CucumberException {

    public UndefinedStepDefinitionException() {
        super("No step definitions found");
    }
}
