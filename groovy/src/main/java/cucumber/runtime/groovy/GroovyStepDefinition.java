package cucumber.runtime.groovy;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Timeout;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import groovy.lang.Closure;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroovyStepDefinition implements StepDefinition {
    private final Pattern pattern;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final int timeoutMillis;
    private final Closure body;
    private final StackTraceElement location;
    private final GroovyBackend backend;
    private List<ParameterType> parameterTypes;

    public GroovyStepDefinition(Pattern pattern, int timeoutMillis, Closure body, StackTraceElement location, GroovyBackend backend) {
        this.pattern = pattern;
        this.timeoutMillis = timeoutMillis;
        this.backend = backend;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.body = body;
        this.parameterTypes = getParameterTypes();
        this.location = location;
    }

    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public Integer getParameterCount() {
        return parameterTypes.size();
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) {
        return parameterTypes.get(n);
    }

    private List<ParameterType> getParameterTypes() {
        Class[] parameterTypes = body.getParameterTypes();
        List<ParameterType> result = new ArrayList<ParameterType>(parameterTypes.length);
        for (Class parameterType : parameterTypes) {
            result.add(new ParameterType(parameterType, null, null));
        }
        return result;
    }

    public void execute(I18n i18n, final Object[] args) throws Throwable {
        Timeout.timeout(new Timeout.Callback<Object>() {
            @Override
            public Object call() throws Throwable {
                backend.invoke(body, args);
                return null;
            }
        }, timeoutMillis);
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public String getPattern() {
        return pattern.pattern();
    }
}
