package cucumber.runtime.groovy;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.StepDefinition;
import cucumber.table.Table;
import cucumber.table.TableConverter;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Step;
import groovy.lang.Closure;

import java.util.List;
import java.util.regex.Pattern;

public class GroovyStepDefinition implements StepDefinition {
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Closure body;
    private final StackTraceElement location;
    private final Pattern pattern;
    private GroovyBackend backend;

    public GroovyStepDefinition(Pattern pattern, Closure body, StackTraceElement location, GroovyBackend backend) {
        this.pattern = pattern;
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

    @Override
    public String getPattern() {
        return pattern.pattern();
    }

    @Override
    public Object tableArgument(int argIndex, List<Row> rows, TableConverter tableConverter) {
        return new Table(rows);
    }
}
