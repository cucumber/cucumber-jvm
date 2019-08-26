package cucumber.runtime.groovy;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Timeout;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.StackTraceUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroovyStepDefinition implements StepDefinition {
    private final Pattern pattern;
    private final long timeoutMillis;
    private final Closure body;
    private final StackTraceElement location;
    private final GroovyBackend backend;

    private final JdkPatternArgumentMatcher argumentMatcher;
    private final List<ParameterInfo> parameterInfos;

    public GroovyStepDefinition(Pattern pattern, long timeoutMillis, Closure body, StackTraceElement location, GroovyBackend backend) {
        this.pattern = pattern;
        this.timeoutMillis = timeoutMillis;
        this.backend = backend;
        this.body = body;
        this.location = location;

        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.parameterInfos = getParameterInfos();
    }

    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public Integer getParameterCount() {
        return parameterInfos.size();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return parameterInfos.get(n);
    }

    private List<ParameterInfo> getParameterInfos() {
        Class[] parameterTypes = body.getParameterTypes();
        return ParameterInfo.fromTypes(parameterTypes);
    }

    public void execute(I18n i18n, final Object[] args) throws Throwable {
        try {
            Timeout.timeout(new Timeout.Callback<Object>() {
                @Override
                public Object call() throws Throwable {
                    backend.invoke(body, args);
                    return null;
                }
            }, timeoutMillis);
        } catch(Throwable e) {
            throw StackTraceUtils.deepSanitize(e);
        }
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public String getPattern() {
        return pattern.pattern();
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
