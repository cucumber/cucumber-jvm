package cucumber.runtime.java;

import cucumber.annotation.Pending;
import cucumber.runtime.CucumberException;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.PendingException;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class JavaStepDefinition implements StepDefinition {
    private final MethodFormat methodFormat;
    private final Method method;
    private final ObjectFactory objectFactory;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Pattern pattern;

    public JavaStepDefinition(Pattern pattern, Method method, ObjectFactory objectFactory) {
        this.pattern = pattern;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodFormat = new MethodFormat();
    }

    public void execute(Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Pending.class)) {
            throw new PendingException(method.getAnnotation(Pending.class).value());
        } 
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

    @Override
    public String getPattern() {
        return pattern.pattern();
    }

    @Override
    public Class getTypeForTableList(int argIndex) {
        Type genericParameterType = method.getGenericParameterTypes()[argIndex];
        if (genericParameterType instanceof ParameterizedType) {
            Type[] parameters = ((ParameterizedType) genericParameterType).getActualTypeArguments();
            return (Class<?>) parameters[0];
        } else {
            return null;
        }
    }
}
