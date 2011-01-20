package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;

import java.util.Collections;
import java.util.List;

public class UndefinedStepRunner extends Match implements StepRunner {
    public UndefinedStepRunner(StackTraceElement stepLocation, List<Integer> matchedColumns) {
        super(Collections.<Argument>emptyList(), stepLocation.getFileName() + ":" + stepLocation.getLineNumber(), matchedColumns);
    }

    public boolean execute(boolean skip, Reporter reporter, StackTraceElement stepLocation) {
        reporter.match(this);
        reporter.result(Result.UNDEFINED);
        return true;
    }
}
