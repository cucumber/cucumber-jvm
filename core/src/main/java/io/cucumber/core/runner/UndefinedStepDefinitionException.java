package io.cucumber.core.runner;

import io.cucumber.core.exception.CucumberException;

final class UndefinedStepDefinitionException extends CucumberException {

    UndefinedStepDefinitionException() {
        super("No step definitions found");
    }

}
