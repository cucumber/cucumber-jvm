package cucumber.runtime.java;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

public class Java8StepDefinition implements StepDefinition {
    public Java8StepDefinition(Pattern compile, long timeoutMillis, StepdefBody body, ObjectFactory objectFactory) {
    }

    @Override
    public List<Argument> matchedArguments(Step step) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocation(boolean detail) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getParameterCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(I18n i18n, Object[] args) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPattern() {
        throw new UnsupportedOperationException();
    }
}
