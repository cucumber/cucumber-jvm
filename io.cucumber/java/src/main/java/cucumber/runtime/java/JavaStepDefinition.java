package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.MethodFormat;
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

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final Pattern pattern;
    private final long timeoutMillis;
    private final ObjectFactory objectFactory;

    private final JdkPatternArgumentMatcher argumentMatcher;
    private final List<ParameterInfo> parameterInfos;

    public JavaStepDefinition(Method method, Pattern pattern, long timeoutMillis, ObjectFactory objectFactory) {
        this.method = method;
        this.pattern = pattern;
        this.timeoutMillis = timeoutMillis;
        this.objectFactory = objectFactory;

        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.parameterInfos = ParameterInfo.fromMethod(method);
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public Integer getParameterCount() {
        return parameterInfos.size();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return parameterInfos.get(n);
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
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
