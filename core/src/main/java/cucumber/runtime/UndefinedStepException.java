package cucumber.runtime;

import gherkin.formatter.model.Step;

class UndefinedStepException extends Throwable {
    public UndefinedStepException(Step step) {
        super(String.format("Undefined Step: %s%s", step.getKeyword(), step.getName()));
    }
}
