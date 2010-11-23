package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CucumberMatch extends Match implements StepRunner {
    private final StepDefinition stepDefinition;

    public CucumberMatch(List<Argument> arguments, StepDefinition stepDefinition) {
        super(arguments, stepDefinition.getLocation());
        this.stepDefinition = stepDefinition;
    }

    public boolean execute(boolean skip, Formatter formatter, StackTraceElement stepLocation) {
        formatter.match(this);
        Result result;
        if(skip) {
            result = new Result("skipped", null);
        } else {
            result = execute(stepDefinition.getParameterTypes(), stepLocation);
        }
        formatter.result(result);
        boolean passed = result.getStatus().equals("passed");
        return !passed;
    }

    private Result execute(Class<?>[] parameterTypes, StackTraceElement stepLocation) {
        String status = "passed";
        Throwable error = null;
        try {
            stepDefinition.execute(getTransformedArgs(parameterTypes));
        } catch(CucumberException e) {
            throw e;
        } catch (InvocationTargetException t) {
            error = t.getTargetException();
            status = "failed";
        } catch (Throwable t) {
            error = t;
            status = "failed";
        }
        String errorMessage = errorMessageWithStackTrace(error, stepLocation);
        return new Result(status, errorMessage);

    }


    private Object[] getTransformedArgs(Class<?>[] parameterTypes) {
        if(parameterTypes.length != getArguments().size()) {
            throw new RuntimeException("Bad number of args"); // TODO: Handle multiline args here...
        }

        Object[] result = new Object[parameterTypes.length];
        int n = 0;
        for (Argument a : getArguments()) {
            // TODO: Use the Locale for transformation
            // TODO: Also use method signature to transform ints...
            result[n++] = a.getVal();
        }
        return result;
    }

    private String errorMessageWithStackTrace(Throwable error, StackTraceElement stepStackTraceElement) {
        if (error == null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        error = filterStacktrace(error, stepStackTraceElement);

        error.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private Throwable filterStacktrace(Throwable error, StackTraceElement stepStackTraceElement) {
        if(error.getCause() != null && error.getCause() != error) {
            return filterStacktrace(error.getCause(), stepStackTraceElement);
        }
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        int stackLength;
        for (stackLength = 1; stackLength < stackTraceElements.length; ++stackLength) {
            if (stepDefinition.isDefinedAt(stackTraceElements[stackLength - 1])) {
                break;
            }
        }
        StackTraceElement[] result = new StackTraceElement[stackLength + 1];
        System.arraycopy(stackTraceElements, 0, result, 0, stackLength);
        result[stackLength] = stepStackTraceElement;
        error.setStackTrace(result);
        return error;
    }
}
