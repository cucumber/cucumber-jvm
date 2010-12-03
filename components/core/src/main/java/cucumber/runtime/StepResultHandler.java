package cucumber.runtime;

import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

public interface StepResultHandler {
    void match(Match match);
    void result(Step step, Result result);
}
