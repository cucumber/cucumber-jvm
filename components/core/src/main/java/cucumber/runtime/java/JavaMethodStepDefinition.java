package cucumber.runtime.java;

import cucumber.StepDefinition;
import cucumber.runtime.CucumberException;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.CucumberMatch;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class JavaMethodStepDefinition implements StepDefinition {
    private final MethodFormat methodFormat;
    private final Method method;
    private final ObjectFactory objectFactory;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Pattern pattern;
    private final Locale locale;

    public JavaMethodStepDefinition(Pattern pattern, Method method, ObjectFactory objectFactory, Locale locale) {
        this.pattern = pattern;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodFormat = new MethodFormat();
        this.locale = locale;
    }

    public Result execute(List<Argument> arguments, StackTraceElement stepStackTraceElement) {
        String status = "passed";
        Throwable error = null;
        try {
            Class<?> type = method.getDeclaringClass();
            Object target = objectFactory.getComponent(type);
            if(target == null) {
                throw new CucumberException("BUG: no component for method " + method);
            }
            method.invoke(target, methodArgs(arguments));
        } catch(CucumberException e) {
            throw e;
        } catch (InvocationTargetException e) {
            error = e.getTargetException();
            status = "failed";
        } catch (Throwable t) {
            error = t;
            status = "failed";
        }
        return new Result(status, errorMessageWithStackTrace(error, stepStackTraceElement));
    }

    public CucumberMatch stepMatch(Step step) {
        List<Argument> arguments = argumentMatcher.argumentsFrom(step.getName());
        return new CucumberMatch(arguments,  methodFormat.format(method), this);
    }

    private String errorMessageWithStackTrace(Throwable error, StackTraceElement stepStackTraceElement) {
        if (error == null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        filterStacktrace(error, stepStackTraceElement);

        error.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private void filterStacktrace(Throwable error, StackTraceElement stepStackTraceElement) {
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        int stackLength;
        for (stackLength = 1; stackLength < stackTraceElements.length; ++stackLength) {
            if (isMethodElement(stackTraceElements[stackLength - 1])) {
                break;
            }
        }
        StackTraceElement[] result = new StackTraceElement[stackLength + 1];
        System.arraycopy(stackTraceElements, 0, result, 0, stackLength);
        result[stackLength] = stepStackTraceElement;
        error.setStackTrace(result);
    }

    private boolean isMethodElement(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    private Object[] methodArgs(List<Argument> arguments) {
        List<Object> result = new ArrayList<Object>();
        for (Argument a : arguments) {
            // TODO: Use the Locale for transformation
            result.add(a.getVal());
        }
        return result.toArray();
    }

    public String toString() {
        return "/" + pattern + "/ -> " + methodFormat.format(method);
    }
}
