package cucumber.runtime.java;

import cucumber.annotation.DateFormat;
import cucumber.annotation.Pending;
import cucumber.runtime.*;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class JavaStepDefinition implements StepDefinition {
    private static final MethodFormat METHOD_FORMAT = new MethodFormat();

    private final Method method;
    private final Pattern pattern;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final ObjectFactory objectFactory;

    public JavaStepDefinition(Method method, Pattern pattern, ObjectFactory objectFactory) {
        this.method = method;
        this.pattern = pattern;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.objectFactory = objectFactory;
    }

    public void execute(Locale locale, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Pending.class)) {
            throw new PendingException(method.getAnnotation(Pending.class).value());
        }
        Class<?> clazz = method.getDeclaringClass();
        Object target = objectFactory.getInstance(clazz);
        try {
            method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            // Can happen if stepdef signature doesn't match args
            throw new CucumberException("Can't invoke " + new MethodFormat().format(method) + " with " + asList(args));
        } catch (InvocationTargetException t) {
            throw t.getTargetException();
        }
    }

    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation() {
        return METHOD_FORMAT.format(method);
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> result = new ArrayList<ParameterType>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String dateFormat = null;
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof DateFormat) {
                    dateFormat = ((DateFormat) annotation).value();
                    break;
                }
            }
            result.add(new ParameterType(genericParameterTypes[i], dateFormat));
        }
        return result;
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return pattern.pattern();
    }
}
