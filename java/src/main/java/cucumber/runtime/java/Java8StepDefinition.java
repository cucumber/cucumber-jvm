package cucumber.runtime.java;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

public class Java8StepDefinition implements StepDefinition {
    private final JavaBackend backend;
    private final Pattern pattern;
    private final long timeoutMillis;
    private final StepdefBody body;
    private final ObjectFactory objectFactory;

    private final JdkPatternArgumentMatcher argumentMatcher;
    private final StackTraceElement location;

    private Method method;
    private final List<ParameterInfo> parameterInfos;

    public Java8StepDefinition(JavaBackend backend, Pattern pattern, long timeoutMillis, StepdefBody body, ObjectFactory objectFactory) {
        this.backend = backend;
        this.pattern = pattern;
        this.timeoutMillis = timeoutMillis;
        this.body = body;
        this.objectFactory = objectFactory;

        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.location = new Exception().getStackTrace()[3];

        Class<? extends StepdefBody> clazz = body.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if(method.getName().equals("accept")) {
                // TODO: Sort or reject the one with Object params.
                this.method = method;
                break;
            }
        }
        this.parameterInfos = ParameterInfo.fromMethod(method);
    }

    @Override
    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    @Override
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public Integer getParameterCount() {
        return pattern.matcher("").groupCount();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) throws IndexOutOfBoundsException {
        return parameterInfos.get(n);
    }

    @Override
    public void execute(final I18n i18n, final Object[] args) throws Throwable {
        Utils.invoke(body, method, timeoutMillis, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public String getPattern() {
        return pattern.pattern();
    }
}
