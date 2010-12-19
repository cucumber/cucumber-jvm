package cucumber.runtime.java;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class JavaStepDefinition implements StepDefinition {
    private final MethodFormat methodFormat;
    private final Method method;
    private final ObjectFactory objectFactory;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Locale locale; // TODO: Use to convert arguments before executions (float etc.) Maybe pass it to execute instead (taking it from the locale of feature instead).

    public JavaStepDefinition(Pattern pattern, Method method, ObjectFactory objectFactory, Locale locale) {
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodFormat = new MethodFormat();
        this.locale = locale;
    }

    public void execute(Object[] args) throws Throwable {
        Object target = objectFactory.getComponent(method.getDeclaringClass());
        method.invoke(target, args);
    }

    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation() {
        return methodFormat.format(method);
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }
}
