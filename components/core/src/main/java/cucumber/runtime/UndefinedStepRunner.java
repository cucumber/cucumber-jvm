package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.Collections;

public class UndefinedStepRunner extends Match implements StepRunner {
    public UndefinedStepRunner(StackTraceElement stepLocation) {
        super(Collections.<Argument>emptyList(), stepLocation.getFileName() + ":" + stepLocation.getLineNumber());
    }

    public boolean execute(boolean skip, Reporter reporter, StackTraceElement stepLocation) {
        reporter.match(this);
        reporter.result(Result.UNDEFINED);
        return true;
    }
}
