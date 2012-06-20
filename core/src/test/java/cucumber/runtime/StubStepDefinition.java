package cucumber.runtime;

import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class StubStepDefinition implements StepDefinition {
    private final Object target;
    private final Method method;
    private final String pattern;
    private List<ParameterType> parameterTypes;

    public StubStepDefinition(Object target, Method method, String pattern) {
        this.target = target;
        this.method = method;
        this.pattern = pattern;
        this.parameterTypes = ParameterType.fromMethod(method);
    }

    @Override
    public List<Argument> matchedArguments(Step step) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocation(boolean detail) {
        return method.getName();
    }

    @Override
    public Integer getParameterCount() {
        return parameterTypes.size();
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) {
        return parameterTypes.get(n);
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
}
