package cucumber.runtime;

import gherkin.I18n;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

public class StubStepDefinition implements StepDefinition {
    private final Object target;
    private final Method method;
    private final String pattern;
    private List<ParameterInfo> parameterInfos;

    public StubStepDefinition(Object target, Method method, String pattern) {
        this.target = target;
        this.method = method;
        this.pattern = pattern;
        this.parameterInfos = ParameterInfo.fromMethod(method);
    }

    @Override
    public List<Argument> matchedArguments(String text) {
        Pattern regexp = Pattern.compile(pattern);
        return new JdkPatternArgumentMatcher(regexp).argumentsFrom(text);
    }

    @Override
    public String getLocation(boolean detail) {
        return method.getName();
    }

    @Override
    public Integer getParameterCount() {
        return parameterInfos.size();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return parameterInfos.get(n);
    }

    @Override
    public void execute(I18n i18n, Object[] args) throws Throwable {
        Utils.invoke(target, method, 0, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
