package cucumber;

import cucumber.runtime.StepMatch;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.List;

public interface StepDefinition {
    Result execute(List<Argument> arguments, StackTraceElement stepStackTraceElement);
    StepMatch stepMatch(Step step);
}
