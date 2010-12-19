package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

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

    public boolean execute(boolean skip, StepResultHandler stepResultHandler, StackTraceElement stepLocation, Step step) {
        stepResultHandler.match(this);
        Result result;
        if (skip) {
            result = Result.SKIPPED;
        } else {
            result = execute(stepDefinition.getParameterTypes(), stepLocation);
        }
        stepResultHandler.result(step, result);
        boolean passed = result.getStatus().equals("passed");
        return !passed;
    }

    private Result execute(Class<?>[] parameterTypes, StackTraceElement stepLocation) {
        String status = "passed";
        Throwable error = null;
        long start = System.currentTimeMillis();
        try {
            stepDefinition.execute(getTransformedArgs(parameterTypes));
        } catch (CucumberException e) {
            throw e;
        } catch (InvocationTargetException t) {
            error = t.getTargetException();
            status = "failed";
        } catch (Throwable t) {
            error = t;
            status = "failed";
        }
        long duration = System.currentTimeMillis() - start;
        String errorMessage = errorMessageWithStackTrace(error, stepLocation);
        return new Result(status, duration, errorMessage);

    }


    private Object[] getTransformedArgs(Class<?>[] parameterTypes) {
        if (parameterTypes != null && parameterTypes.length != getArguments().size()) {
            throw new CucumberException("Bad number of args"); // TODO: Handle multiline args here...
        }

        Object[] result = new Object[getArguments().size()];
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
        if (error.getCause() != null && error.getCause() != error) {
            return filterStacktrace(error.getCause(), stepStackTraceElement);
        }
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        if (stackTraceElements.length == 0) {
            return error;
        }
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
