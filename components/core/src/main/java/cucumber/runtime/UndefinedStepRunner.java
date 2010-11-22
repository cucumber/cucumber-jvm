package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;

import java.util.Collections;

public class UndefinedStepRunner extends Match implements StepRunner {
    private static final Result UNDEFINED = new Result("undefined", null);

    public UndefinedStepRunner(StackTraceElement stepLocation) {
        super(Collections.<Argument>emptyList(), stepLocation.getFileName() + ":" + stepLocation.getLineNumber());
    }

    public boolean execute(boolean skip, Formatter formatter, StackTraceElement stepLocation) {
        formatter.match(this);
        formatter.result(UNDEFINED);
        return true;
    }
}
