package cucumber.runtime.java;

import cucumber.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;

import java.lang.reflect.Method;
import java.util.List;

public class MethodStepDefinition implements StepDefinition {
    private MethodFormat methodFormat;
    private Method method;

    public MethodStepDefinition(Method method, Object target) {
        this.method = method;
        this.methodFormat = new MethodFormat();
    }

    public Result execute(List<Argument> arguments) {
        return new Result("passed", null, arguments, methodFormat.format(method));
    }
}
