package cucumber.runtime.groovy;

import cucumber.StepDefinition;
import cucumber.runtime.JdkPatternArgumentMatcher;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import groovy.lang.Closure;

import java.util.List;
import java.util.regex.Pattern;

public class GroovyStepDefinition implements StepDefinition {
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Closure body;
    private final StackTraceElement location;
    private GroovyBackend backend;

    public GroovyStepDefinition(Pattern pattern, Closure body, StackTraceElement location, GroovyBackend backend) {
        this.backend = backend;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.body = body;
        this.location = location;
    }

    public List<Argument> argumentsFrom(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation() {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    public Class<?>[] getParameterTypes() {
        return body.getParameterTypes();
    }

    public void execute(Object[] args) throws Throwable {
        backend.invokeStepDefinition(body, args);
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }
}
