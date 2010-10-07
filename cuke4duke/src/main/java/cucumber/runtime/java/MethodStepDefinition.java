package cucumber.runtime.java;

import cucumber.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;

import java.lang.reflect.Method;
import java.util.List;

public class MethodStepDefinition implements StepDefinition {
    public MethodStepDefinition(Method method, Object target) {
        
    }

    public Result execute(List<Argument> arguments) {
        return new Result("passed", null, arguments, "CukeSteps.haveNCukes(String)");
    }
}
