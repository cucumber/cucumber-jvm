package cucumber.runtime;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.table.Table;
import cucumber.table.TableConverter;
import cucumber.table.TableHeaderMapper;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

public class StepDefinitionMatch extends Match {
    private final StepDefinition stepDefinition;
    private final transient String uri;
    // The official JSON gherkin format doesn't have a step attribute, so we're marking this as transient
    // to prevent it from ending up in the JSON.
    private final transient Step step;
    private final LocalizedXStreams localizedXStreams;
    private final TableHeaderMapper tableHeaderMapper;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, String uri, Step step, LocalizedXStreams localizedXStreams, TableHeaderMapper tableHeaderMapper) {
        super(arguments, stepDefinition.getLocation());
        this.stepDefinition = stepDefinition;
        this.uri = uri;
        this.step = step;
        this.localizedXStreams = localizedXStreams;
        this.tableHeaderMapper = tableHeaderMapper;
    }

    public void runStep(Locale locale) throws Throwable {
        if (locale == null) {
            throw new NullPointerException("null Locale!");
        }
        try {
            Object[] args = transformedArgs(stepDefinition.getParameterTypes(), step, localizedXStreams.get(locale));
            stepDefinition.execute(args);
        } catch (CucumberException e) {
            throw e;
        } catch (InvocationTargetException t) {
            throw filterStacktrace(t.getTargetException(), getStepLocation());
        } catch (Throwable t) {
            throw filterStacktrace(t, getStepLocation());
        }
    }

    /**
     * @param parameterTypes types of the stepdefs args. Some backends will pass null if they can't determine types or arity.
     * @param step
     * @param xStream
     * @return an Array matching the types or {@code parameterTypes}, or an array of String if {@code parameterTypes} is null
     */
    private Object[] transformedArgs(Class<?>[] parameterTypes, Step step, XStream xStream) {
        int argumentCount = getArguments().size();
        if (step.getDocString() != null) argumentCount++;
        if (step.getRows() != null) argumentCount++;
        if (parameterTypes != null && parameterTypes.length != argumentCount) {
            throw new CucumberException("Arity mismatch. Parameters: " + asList(parameterTypes) + ". Matched arguments: " + getArguments());
        }

        Object[] result = new Object[argumentCount];
        ConverterLookup converterLookup = xStream.getConverterLookup();

        int n = 0;
        for (Argument a : getArguments()) {
            if (parameterTypes != null) {
                // TODO: We might get a lookup that doesn't implement SingleValueConverter
                // Need to throw a more friendly exception in that case.
                SingleValueConverter converter = (SingleValueConverter) converterLookup.lookupConverterForType(parameterTypes[n]);
                result[n] = converter.fromString(a.getVal());
            } else {
                result[n] = a.getVal();
            }
            n++;
        }

        if (step.getRows() != null) {
            result[n] = tableArgument(step, n++, xStream);
        } else if (step.getDocString() != null) {
            result[n] = step.getDocString().getValue();
        }
        return result;
    }

    private Object tableArgument(Step step, int argIndex, XStream xStream) {
        Table table = new Table(step.getRows(), new TableConverter(xStream), tableHeaderMapper);

        Type listType = stepDefinition.getTypeForTableList(argIndex);
        if (listType != null) {
            return table.asList(listType);
        } else {
            return table;
        }
    }

    public Throwable filterStacktrace(Throwable error, StackTraceElement stepLocation) {
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
        if (stepLocation != null) {
            StackTraceElement[] result = new StackTraceElement[stackLength + 1];
            System.arraycopy(stackTraceElements, 0, result, 0, stackLength);
            result[stackLength] = stepLocation;
            error.setStackTrace(result);
        }
        return error;
    }

    public String getPattern() {
        return stepDefinition.getPattern();
    }

    public StackTraceElement getStepLocation() {
        return step.getStackTraceElement(uri);
    }
}
