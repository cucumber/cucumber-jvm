package cucumber.runtime;

import cucumber.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.List;

public class StepMatch {
    private final StepDefinition stepDefinition;
    private final Step step;
    private final List<Argument> arguments;

    public StepMatch(StepDefinition stepDefinition, List<Argument> arguments, Step step) {
        this.stepDefinition = stepDefinition;
        this.step = step;
        this.arguments = arguments;
    }

    public void execute(Formatter formatter, StackTraceElement stepStackTraceElement) {
        Result result = stepDefinition.execute(arguments, stepStackTraceElement);
        Step executedStep = new Step(step.getComments(), step.getKeyword(), step.getName(), step.getLine(), step.getMultilineArg(), result);
        formatter.step(executedStep);
    }
}
