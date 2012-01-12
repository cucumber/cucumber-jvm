package cucumber.runtime;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.converters.DateConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.converters.SingleValueConverterWrapperExt;
import cucumber.table.DataTable;
import cucumber.table.TableConverter;
import gherkin.formatter.Argument;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;
import gherkin.util.Mapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static gherkin.util.FixJava.map;

public class StepDefinitionMatch extends Match {
    private final StepDefinition stepDefinition;
    private final transient String uri;
    // The official JSON gherkin format doesn't have a step attribute, so we're marking this as transient
    // to prevent it from ending up in the JSON.
    private final transient Step step;
    private final LocalizedXStreams localizedXStreams;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, String uri, Step step, LocalizedXStreams localizedXStreams) {
        super(arguments, stepDefinition.getLocation());
        this.stepDefinition = stepDefinition;
        this.uri = uri;
        this.step = step;
        this.localizedXStreams = localizedXStreams;
    }

    public void runStep(Locale locale) throws Throwable {
        try {
            Object[] args = transformedArgs(stepDefinition.getParameterTypes(), step, localizedXStreams.get(locale), locale);
            stepDefinition.execute(locale, args);
        } catch (CucumberException e) {
            throw e;
        } catch (Throwable t) {
            throw filterStacktrace(t, getStepLocation());
        }
    }

    /**
     * @param parameterTypes types of the stepdefs args. Some backends will pass null if they can't determine types or arity.
     * @param step           the step to run
     * @param xStream        used to convert a string to declared stepdef arguments
     * @param locale         the feature's locale
     * @return an Array matching the types or {@code parameterTypes}, or an array of String if {@code parameterTypes} is null
     */
    private Object[] transformedArgs(List<ParameterType> parameterTypes, Step step, XStream xStream, Locale locale) {
        if (xStream == null) {
            throw new NullPointerException("xStream");
        }
        int argumentCount = getArguments().size();
        if (step.getDocString() != null) argumentCount++;
        if (step.getRows() != null) argumentCount++;
        if (parameterTypes != null) {
            if (parameterTypes.size() != argumentCount) {
                List<Argument> arguments = createArgumentsForErrorMessage(step);
                throw new CucumberException("Arity mismatch. Declared parameters: " + parameterTypes + ". Matched arguments: " + arguments);
            }
        } else {
            // Some backends, like ClojureBackend, don't know the arity and therefore pass in null.
            parameterTypes = Utils.listOf(argumentCount, new ParameterType(String.class, null));
        }

        Object[] result = new Object[argumentCount];
        ConverterLookup converterLookup = xStream.getConverterLookup();

        int n = 0;
        for (Argument a : getArguments()) {
            SingleValueConverter converter;
            ParameterType parameterType = parameterTypes.get(n);
            if (parameterType.getDateFormat() != null) {
                converter = new DateConverter(parameterType.getDateFormat(), locale);
            } else {
                // TODO: We might get a lookup that doesn't implement SingleValueConverter
                // Need to throw a more friendly exception in that case.
                converter = (SingleValueConverter) converterLookup.lookupConverterForType(parameterType.getParameterClass());
            }
            result[n] = converter.fromString(a.getVal());
            n++;
        }

        if (step.getRows() != null) {
            ParameterType parameterType = parameterTypes.get(n);
            DateConverter dateConverter = null;
            if (parameterType.getDateFormat() != null) {
                SingleValueConverterWrapperExt converterWrapper = (SingleValueConverterWrapperExt) xStream.getConverterLookup().lookupConverterForType(Date.class);
                dateConverter = (DateConverter) converterWrapper.getConverter();
                dateConverter.setOnlyFormat(parameterType.getDateFormat(), locale);
            }
            try {
                result[n] = tableArgument(step, n, xStream);
            } finally {
                if (dateConverter != null) {
                    dateConverter.removeOnlyFormat();
                }
            }
        } else if (step.getDocString() != null) {
            result[n] = step.getDocString().getValue();
        }
        return result;
    }

    private List<Argument> createArgumentsForErrorMessage(Step step) {
        List<Argument> arguments = new ArrayList<Argument>(getArguments());
        if (step.getDocString() != null) {
            arguments.add(new Argument(-1, "DocString:" + step.getDocString().getValue()));
        }
        if (step.getRows() != null) {
            List<List<String>> rows = map(step.getRows(), new Mapper<DataTableRow, List<String>>() {
                @Override
                public List<String> map(DataTableRow row) {
                    return row.getCells();
                }
            });
            arguments.add(new Argument(-1, "Table:" + rows.toString()));
        }
        return arguments;
    }

    private Object tableArgument(Step step, int argIndex, XStream xStream) {
        DataTable table = new DataTable(step.getRows(), new TableConverter(xStream));

        Type listType = getGenericListType(argIndex);
        if (listType != null) {
            return table.asList(listType);
        } else {
            return table;
        }
    }

    private Type getGenericListType(int argIndex) {
        ParameterType parameterType = stepDefinition.getParameterTypes().get(argIndex);
        Type[] actualTypeArguments = parameterType.getActualTypeArguments();
        return actualTypeArguments != null && actualTypeArguments.length > 0 ? actualTypeArguments[0] : null;
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
