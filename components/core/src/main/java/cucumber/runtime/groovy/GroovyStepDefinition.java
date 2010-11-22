package cucumber.runtime.groovy;

import cucumber.StepDefinition;
import cucumber.runtime.CucumberMatch;
import cucumber.runtime.JdkPatternArgumentMatcher;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import groovy.lang.Closure;

import java.util.List;
import java.util.regex.Pattern;

public class GroovyStepDefinition implements StepDefinition {
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Closure body;
    private final StackTraceElement location;

    public GroovyStepDefinition(Pattern pattern, Closure body, StackTraceElement location) {
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.body = body;
        this.location = location;
    }

    public Result execute(List<Argument> arguments, StackTraceElement stepStackTraceElement) {
        throw new UnsupportedOperationException();
    }

    public CucumberMatch stepMatch(Step step) {
        List<Argument> arguments = argumentMatcher.argumentsFrom(step.getName());
        return new CucumberMatch(arguments, this);
    }

    public String getLocation() {
        return location.getFileName() + ":" + location.getLineNumber();
    }
}
