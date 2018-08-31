package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;

final class UndefinedStepException extends Throwable {
    public UndefinedStepException(PickleStep step) {
        super(String.format("Undefined Step: %s", step.getText()));
    }
}
