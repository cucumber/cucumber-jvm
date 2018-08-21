package cucumber.runner;

import cucumber.runtime.CucumberException;

class UndefinedStepDefinitionException extends CucumberException {

    UndefinedStepDefinitionException() {
        super("No step definitions found");
    }
}
