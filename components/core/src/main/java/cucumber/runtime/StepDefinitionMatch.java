package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.util.Arrays.asList;

public class StepDefinitionMatch extends Match implements StepRunner {
    private final StepDefinition stepDefinition;
    private final Step step;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, Step step, List<Integer> matchedColumns) {
        super(arguments, stepDefinition.getLocation(), matchedColumns);
        this.stepDefinition = stepDefinition;
        this.step = step;
    }

    public boolean execute(boolean skip, Reporter reporter, StackTraceElement stepLocation) {
        reporter.match(this);
        Result result;
        if (skip) {
            result = Result.SKIPPED;
        } else {
            result = execute(stepDefinition.getParameterTypes(), stepLocation);
        }
        reporter.result(result);
        boolean passed = result.getStatus().equals("passed");
        return !passed;
    }

    public boolean canRun() {
        return true;
    }

    public void run() throws Throwable {
        try {
            stepDefinition.execute(getTransformedArgs(stepDefinition.getParameterTypes()));
        } catch (CucumberException e) {
            throw e;
        } catch (InvocationTargetException t) {
            throw filterStacktrace(t.getTargetException(), step.getStackTraceElement());
        } catch (Throwable t) {
            throw filterStacktrace(t, step.getStackTraceElement());
        }
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
            throw new CucumberException("Arity mismatch. Parameters: " + asList(parameterTypes) + ". Matched arguments: " + getArguments()); // TODO: Handle multiline args here...
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

    private String errorMessageWithStackTrace(Throwable error, StackTraceElement stepLocation) {
        if (error == null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        error = filterStacktrace(error, stepLocation);

        error.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private Throwable filterStacktrace(Throwable error, StackTraceElement stepLocation) {
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        if (error.getCause() != null && error.getCause() != error) {
            return filterStacktrace(error.getCause(), stepLocation);
        }
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
        result[stackLength] = stepLocation;
        error.setStackTrace(result);
        return error;
    }
}
