package cucumber.runtime.java;

import cucumber.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Result;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodStepDefinition implements StepDefinition {
    private MethodFormat methodFormat;
    private Method method;
    private final Object target;

    public MethodStepDefinition(Method method, Object target) {
        this.method = method;
        this.target = target;
        this.methodFormat = new MethodFormat();
    }

    public Result execute(List<Argument> arguments, StackTraceElement stepStackTraceElement) {
        String status = "passed";
        Throwable error = null;
        try {
            method.invoke(target, methodArgs(arguments));
        } catch (InvocationTargetException e) {
            error = e.getTargetException();
            status = "failed";
        } catch (Throwable t) {
            error = t;
            status = "failed";
        }
        return new Result(status, stackTrace(error, stepStackTraceElement), arguments, methodFormat.format(method));
    }

    private String stackTrace(Throwable error, StackTraceElement stepStackTraceElement) {
        if(error == null) return null;
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
        for(stackLength = 1; stackLength < stackTraceElements.length; ++stackLength) {
            if(isMethodElement(stackTraceElements[stackLength-1])) {
                break;
            }
        }
        StackTraceElement[] result = new StackTraceElement[stackLength+1];
        System.arraycopy(stackTraceElements, 0, result, 0, stackLength);
        result[stackLength] = stepStackTraceElement;
        error.setStackTrace(result);
    }

    private boolean isMethodElement(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    private Object[] methodArgs(List<Argument> arguments) {
        List<Object> result = new ArrayList<Object>();
        for(Argument a : arguments) {
            result.add(a.getVal());
        }
        return result.toArray();
    }
}
