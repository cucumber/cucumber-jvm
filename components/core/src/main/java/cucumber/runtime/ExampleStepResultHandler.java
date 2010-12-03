package cucumber.runtime;

import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

public class ExampleStepResultHandler implements StepResultHandler {
    private final ExecuteFormatter executeFormatter;

    public ExampleStepResultHandler(ExecuteFormatter executeFormatter) {
        this.executeFormatter = executeFormatter;
    }

    public void match(Match match) {
        // Nothing to do
    }

    public void result(Step step, Result result) {
        executeFormatter.exampleResult(step, result);
    }
}
