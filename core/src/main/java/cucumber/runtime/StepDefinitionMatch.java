package cucumber.runtime;

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.table.Table;
import cucumber.table.TableConverter;
import cucumber.table.TableHeaderMapper;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Step;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

public class StepDefinitionMatch extends Match {
    private final StepDefinition stepDefinition;
    private final String uri;
    private final Step step;
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
            Object[] args = transformedArgs(stepDefinition.getParameterTypes(), step, locale);
            stepDefinition.execute(args);
        } catch (CucumberException e) {
            throw e;
        } catch (InvocationTargetException t) {
            throw filterStacktrace(t.getTargetException(), getStepLocation());
        } catch (Throwable t) {
            throw filterStacktrace(t, getStepLocation());
        }
    }

    private Object[] transformedArgs(Class<?>[] parameterTypes, Step step, Locale locale) {
        int argumentCount = getArguments().size() + (step.getMultilineArg() == null ? 0 : 1);
        if (parameterTypes.length != argumentCount) {
            throw new CucumberException("Arity mismatch. Parameters: " + asList(parameterTypes) + ". Matched arguments: " + getArguments());
        }

        Object[] result = new Object[argumentCount];
        int n = 0;
        if (step.getRows() != null) {
            TableConverter tableConverter = new TableConverter(localizedXStreams.get(locale));
            result[n] = tableArgument(step, tableConverter, n++);
        } else if (step.getDocString() != null) {
            result[n] = step.getDocString().getValue();
        } else {
            ConverterLookup converterLookup = localizedXStreams.get(locale).getConverterLookup();
            for (Argument a : getArguments()) {
                // TODO: We might get a lookup that doesn't implement SingleValueConverter
                // Need to throw a more friendly exception in that case.
                SingleValueConverter converter = (SingleValueConverter) converterLookup.lookupConverterForType(parameterTypes[n]);
                result[n] = converter.fromString(a.getVal());
                n++;
            }
        }
        return result;
    }

    private Object tableArgument(Step step, TableConverter tableConverter, int argIndex) {
        Class listType = stepDefinition.getTypeForTableList(argIndex);
        if(listType != null) {
            return tableConverter.convert(listType, attributeNames(step.getRows()), attributeValues(step.getRows()));
        } else {
            return new Table(step.getRows());
        }
    }

    private List<List<String>> attributeValues(List<Row> rows) {
        List<List<String>> attributeValues = new ArrayList<List<String>>();
        List<Row> valueRows = rows.subList(1, rows.size());
        for (Row valueRow : valueRows) {
            attributeValues.add(toStrings(valueRow));
        }
        return attributeValues;
    }

    private List<String> attributeNames(List<Row> rows) {
        List<String> strings = new ArrayList<String>();
        for (String string : rows.get(0).getCells()) {
            strings.add(tableHeaderMapper.map(string));
        }
        return strings;
    }

    private List<String> toStrings(Row row) {
        List<String> strings = new ArrayList<String>();
        for (String string : row.getCells()) {
            strings.add(string);
        }
        return strings;
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
        if(stepLocation != null) {
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
