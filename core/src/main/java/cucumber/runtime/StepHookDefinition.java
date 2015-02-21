package cucumber.runtime;

import gherkin.formatter.model.Step;

public interface StepHookDefinition extends Hook {
    void execute(Step step) throws Throwable;
}