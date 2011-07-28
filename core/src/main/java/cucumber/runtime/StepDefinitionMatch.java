package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

import cucumber.runtime.transformers.Transformer;

import static java.util.Arrays.asList;

public class StepDefinitionMatch extends Match {
    private final StepDefinition stepDefinition;
    private final Step step;
	private Transformer transformer;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, Step step, Transformer transformer) {
        super(arguments, stepDefinition.getLocation());
        this.stepDefinition = stepDefinition;
        this.step = step;
        this.transformer = transformer;
    }

    public void run(String path) throws Throwable {
        try {
            stepDefinition.execute(getTransformedArgs(stepDefinition.getParameterTypes()));
        } catch (CucumberException e) {
            throw e;
        } catch (InvocationTargetException t) {
            throw filterStacktrace(t.getTargetException(), step.getStackTraceElement(path));
        } catch (Throwable t) {
            throw filterStacktrace(t, step.getStackTraceElement(path));
        }
    }

    private Object[] getTransformedArgs(Class<?>[] parameterTypes) {
        if (parameterTypes != null && parameterTypes.length != getArguments().size()) {
            throw new CucumberException("Arity mismatch. Parameters: " + asList(parameterTypes) + ". Matched arguments: " + getArguments()); // TODO: Handle multiline args here...
        }

        Object[] result = new Object[getArguments().size()];
        int n = 0;
        for (Argument a : getArguments()) {
            result[n] = this.transformer.transform(a, parameterTypes[n++], getLocale());
        }
        return result;
    }

	private Locale getLocale() {
		return this.stepDefinition.getLocale();
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
