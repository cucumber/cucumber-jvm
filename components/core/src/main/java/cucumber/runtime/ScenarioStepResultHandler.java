package cucumber.runtime;

import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

public class ScenarioStepResultHandler implements StepResultHandler {
    private final ExecuteFormatter executeFormatter;

    public ScenarioStepResultHandler(ExecuteFormatter executeFormatter) {
        this.executeFormatter = executeFormatter;
    }

    public void match(Match match) {
        executeFormatter.scenarioStepMatch(match);
    }

    public void result(Step step, Result result) {
        executeFormatter.scenarioStepResult(result);
    }
}
