package cucumber.runtime.java;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final Pattern pattern;
    private final int timeout;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final ObjectFactory objectFactory;
    private List<ParameterType> parameterTypes;

    public JavaStepDefinition(Method method, Pattern pattern, int timeout, ObjectFactory objectFactory) {
        this.method = method;
        this.parameterTypes = ParameterType.fromMethod(method);
        this.pattern = pattern;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.timeout = timeout;
        this.objectFactory = objectFactory;
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeout, args);
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
        return parameterTypes.size();
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) {
        return parameterTypes.get(n);
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return pattern.pattern();
    }

    boolean isAnnotatedWithOneOf(List<Class<? extends Annotation>> advises) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (advises.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }
}
