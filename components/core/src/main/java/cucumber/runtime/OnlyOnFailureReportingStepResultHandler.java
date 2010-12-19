package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

public class OnlyOnFailureReportingStepResultHandler implements StepResultHandler {
    public OnlyOnFailureReportingStepResultHandler(Reporter reporter) {
    }

    public void match(Match match) {
    }

    public void result(Step step, Result result) {
    }
}
