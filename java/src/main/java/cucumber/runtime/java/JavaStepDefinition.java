package cucumber.runtime.java;

import cucumber.runtime.AbstractStepDefinition;
import cucumber.runtime.CucumberException;
import cucumber.runtime.JdkPatternArgumentMatcher;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class JavaStepDefinition extends AbstractStepDefinition {
    private final MethodFormat methodFormat;
    private final Method method;
    private final ObjectFactory objectFactory;
    private final JdkPatternArgumentMatcher argumentMatcher;

    public JavaStepDefinition(Pattern pattern, Method method, ObjectFactory objectFactory, Locale locale) {
        super(locale);
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodFormat = new MethodFormat();
    }

    public void execute(Object[] args) throws Throwable {
        Object target = objectFactory.getInstance(method.getDeclaringClass());
        try {
            method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            // Can happen if stepdef signature doesn't match args
            throw new CucumberException("Can't invoke " + new MethodFormat().format(method) + " with " + asList(args));
        }
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
