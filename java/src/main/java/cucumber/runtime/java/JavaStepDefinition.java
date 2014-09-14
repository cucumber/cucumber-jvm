package cucumber.runtime.java;

import cucumber.api.Transpose;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final Pattern pattern;
    private final long timeout;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final ObjectFactory objectFactory;
    private final boolean isTranspose;
    private List<ParameterInfo> parameterInfos;

    public JavaStepDefinition(Method method, Pattern pattern, long timeoutMillis, ObjectFactory objectFactory) {
        this.method = method;
        this.parameterInfos = ParameterInfo.fromMethod(method);
        this.pattern = pattern;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.timeout = timeoutMillis;
        this.objectFactory = objectFactory;
        this.isTranspose = method.getParameterTypes().length == 1 && hasAnnotation(method.getParameterAnnotations()[0], Transpose.class);
    }

    private static boolean hasAnnotation(final Annotation[] list, Class<? extends Annotation> expected) {
        for (final Annotation a : list) {
            if (a.annotationType() == expected) {
                return true;
            }
        }
        return false;
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
        if (isTranspose && args != null && List.class.isInstance(args[0]) && !List.class.isAssignableFrom(method.getParameterTypes()[0])) {
            for (final Object o : List.class.cast(args[0])) {
                Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeout, new Object[] { o });
            }
        } else {
            Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeout, args);
        }
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
}
