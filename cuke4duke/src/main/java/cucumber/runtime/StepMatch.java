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
    private final StackTraceElement stepStackTraceElement;

    public StepMatch(StepDefinition stepDefinition, List<Argument> arguments, Step step, String featureUri, String featureName, String elementName) {
        this.stepDefinition = stepDefinition;
        this.step = step;
        this.arguments = arguments;
        this.stepStackTraceElement = new StackTraceElement(featureName + "." + elementName, step.getKeyword()+step.getName(), featureUri, step.getLine());
    }

    public void execute(Formatter formatter) {
        Result result = stepDefinition.execute(arguments, stepStackTraceElement);
        Step executedStep = new Step(step.getComments(), step.getKeyword(), step.getName(), step.getLine(), step.getMultilineArg(), result);
        formatter.step(executedStep);
    }
}
