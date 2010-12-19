package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

public class ReportingStepResultHandler implements StepResultHandler {
    private Reporter reporter;

    public ReportingStepResultHandler(Reporter reporter) {
        this.reporter = reporter;
    }

    public void match(Match match) {
        reporter.match(match);
    }

    public void result(Step step, Result result) {
        reporter.result(result);
    }
}
