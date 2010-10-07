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

    public Result execute(List<Argument> arguments, String stepLocation) {
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
        return new Result(status, stackTrace(error, stepLocation), arguments, methodFormat.format(method));
    }

    private String stackTrace(Throwable error, String stepLocation) {
        if(error == null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println(error);
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        for(StackTraceElement e : stackTraceElements) {
            pw.println("\tat " + e);
            break;
        }
        pw.println("\tat " + stepLocation);

        pw.flush();
        return sw.toString();
    }

    private Object[] methodArgs(List<Argument> arguments) {
        List<Object> result = new ArrayList<Object>();
        for(Argument a : arguments) {
            result.add(a.getVal());
        }
        return result.toArray();
    }
}
