package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.Collections;

public class UndefinedStepRunner extends Match implements StepRunner {
    public UndefinedStepRunner(StackTraceElement stepLocation) {
        super(Collections.<Argument>emptyList(), stepLocation.getFileName() + ":" + stepLocation.getLineNumber());
    }

    public boolean execute(boolean skip, StepResultHandler stepResultHandler, StackTraceElement stepLocation, Step step) {
        stepResultHandler.match(this);
        stepResultHandler.result(step, Result.UNDEFINED);
        return true;
    }
}
