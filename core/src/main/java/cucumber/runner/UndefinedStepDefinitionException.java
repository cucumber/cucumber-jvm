package cucumber.runner;

import cucumber.runtime.CucumberException;

final class UndefinedStepDefinitionException extends CucumberException {

    UndefinedStepDefinitionException() {
        super("No step definitions found");
    }
}
