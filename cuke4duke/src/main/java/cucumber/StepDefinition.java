package cucumber;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;

import java.util.List;

public interface StepDefinition {
    Result execute(List<Argument> arguments, String stepLocation);
}
