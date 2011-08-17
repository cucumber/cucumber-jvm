package cucumber.runtime;

import cucumber.runtime.transformers.TransformationException;
import cucumber.runtime.transformers.Transformers;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

public class StepDefinitionMatch extends Match {
    private final StepDefinition stepDefinition;
    private final Step step;
    private Transformers transformers;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, Step step, Transformers transformers) {
        super(arguments, stepDefinition.getLocation());
        this.stepDefinition = stepDefinition;
        this.step = step;
        this.transformers = transformers;
    }

    public void runStep(Step step, String stackTracePath, Locale locale) throws Throwable {
        try {
            stepDefinition.execute(transformedArgs(stepDefinition.getParameterTypes(), step, locale));
        } catch (CucumberException e) {
            throw e;
        } catch (InvocationTargetException t) {
            throw filterStacktrace(t.getTargetException(), this.step.getStackTraceElement(stackTracePath));
        } catch (Throwable t) {
            throw filterStacktrace(t, this.step.getStackTraceElement(stackTracePath));
        }
    }

    private Object[] transformedArgs(Class<?>[] parameterTypes, Step step, Locale locale) throws TransformationException {
        int argumentCount = getArguments().size() + (step.getMultilineArg() == null ? 0 : 1);
        if (parameterTypes.length != argumentCount) {
            throw new CucumberException("Arity mismatch. Parameters: " + asList(parameterTypes) + ". Matched arguments: " + getArguments());
        }

        Object[] result = new Object[argumentCount];
        int n = 0;
        for (Argument a : getArguments()) {
            result[n] = transformers.transform(locale, parameterTypes[n++], a.getVal());
        }
        if (step.getDocString() != null) {
            result[n] = step.getDocString().getValue();
        }
        if (step.getRows() != null) {
            // TODO: This should be a cucumber.Table, which will wrap the data in the rows, providing a similar API to the ruby impl (especially diffing)
            result[n] = step.getRows();
        }
        return result;
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
