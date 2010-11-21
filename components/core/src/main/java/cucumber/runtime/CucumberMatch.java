package cucumber.runtime;

import cucumber.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;

import java.util.List;

public class CucumberMatch extends Match {
    private final StepDefinition stepDefinition;

    public CucumberMatch(List<Argument> arguments, String location, StepDefinition stepDefinition) {
        super(arguments, location);
        this.stepDefinition = stepDefinition;
    }

    public boolean execute(boolean skip, Formatter formatter, StackTraceElement stepStackTraceElement) {
        formatter.match(this);
        Result result;
        if(skip) {
            result = new Result("skipped", null);
        } else {
            result = stepDefinition.execute(getArguments(), stepStackTraceElement);
        }
        formatter.result(result);
        return result.getStatus().equals("passed");
    }
}
