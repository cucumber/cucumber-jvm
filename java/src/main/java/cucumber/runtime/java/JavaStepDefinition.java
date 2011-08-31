package cucumber.runtime.java;

import cucumber.annotation.TableProcessorInfo;
import cucumber.runtime.CucumberException;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.StepDefinition;
import cucumber.table.Table;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public Object tableArgument(int argIndex, Table table) {
        Annotation[] annotations = this.method.getParameterAnnotations()[argIndex];
        for (Annotation annotation : annotations) {
            if (annotation instanceof TableProcessorInfo) {
                return newTableArgumentProcessor((TableProcessorInfo) annotation).process(table);
            } else if (annotation.annotationType().isAnnotationPresent(TableProcessorInfo.class)) {
                return newTableArgumentProcessor(annotation.annotationType().getAnnotation(TableProcessorInfo.class), annotation).process(table);
            }
        }
        return table;
    }

    private TableProcessor newTableArgumentProcessor(TableProcessorInfo tableProcessorInfo, Annotation extraInfo) {
        try {
            Method valueMethod = extraInfo.annotationType().getMethod("value");
            Constructor<? extends TableProcessor> constructor = (Constructor<? extends TableProcessor>) tableProcessorInfo.processorClass().getConstructor(valueMethod.getReturnType());
            return constructor.newInstance(valueMethod.invoke(extraInfo));
        } catch (IllegalArgumentException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        } catch (InstantiationException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        } catch (IllegalAccessException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        } catch (InvocationTargetException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        } catch (SecurityException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        } catch (NoSuchMethodException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        }
    }

    private TableProcessor newTableArgumentProcessor(TableProcessorInfo tableProcessorInfo) {
        try {
            return (TableProcessor) tableProcessorInfo.processorClass().newInstance();
        } catch (InstantiationException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        } catch (IllegalAccessException e) {
            throw new CucumberException("Error instantiating " + tableProcessorInfo.processorClass(), e);
        }
    }
}
