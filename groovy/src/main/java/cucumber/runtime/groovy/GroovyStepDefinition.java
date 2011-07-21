package cucumber.runtime.groovy;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import groovy.lang.Closure;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import cucumber.runtime.AbstractStepDefinition;
import cucumber.runtime.JdkPatternArgumentMatcher;

public class GroovyStepDefinition extends AbstractStepDefinition {
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Closure body;
    private final StackTraceElement location;
    private GroovyBackend backend;

    public GroovyStepDefinition(Pattern pattern, Closure body, StackTraceElement location, GroovyBackend backend, Locale locale) {
    	super(locale);
        this.backend = backend;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.body = body;
        this.location = location;
    }

    public List<Argument> matchedArguments(Step step) {
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
