package cucumber.runtime.java;

import cucumber.StepDefinition;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.StepMatch;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JavaMethodStepDefinition implements StepDefinition {
    private final MethodFormat methodFormat;
    private final Method method;
    private final ObjectFactory objectFactory;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Pattern pattern;

    public JavaMethodStepDefinition(Pattern pattern, Method method, ObjectFactory objectFactory) {
        this.pattern = pattern;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodFormat = new MethodFormat();
    }

    public Result execute(List<Argument> arguments, StackTraceElement stepStackTraceElement) {
        String status = "passed";
        Throwable error = null;
        try {
            method.invoke(objectFactory.getComponent(method.getDeclaringClass()), methodArgs(arguments));
        } catch (InvocationTargetException e) {
            error = e.getTargetException();
            status = "failed";
        } catch (Throwable t) {
            error = t;
            status = "failed";
        }
        return new Result(status, stackTrace(error, stepStackTraceElement), arguments, methodFormat.format(method));
    }

    public StepMatch stepMatch(Step step) {
        List<Argument> arguments = argumentMatcher.argumentsFrom(step.getName());
        return new StepMatch(this, arguments, step);
    }

    private String stackTrace(Throwable error, StackTraceElement stepStackTraceElement) {
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
            result.add(a.getVal());
        }
        return result.toArray();
    }

    public String toString() {
        return "/" + pattern + "/ -> " + methodFormat.format(method);
    }
}
