package cucumber.runtime;

import cucumber.api.DataTable;
import cucumber.runtime.table.TableConverter;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;
import gherkin.util.Mapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static gherkin.util.FixJava.map;

public class StepDefinitionMatch extends Match {
    private final StepDefinition stepDefinition;
    private final transient String featurePath;
    // The official JSON gherkin format doesn't have a step attribute, so we're marking this as transient
    // to prevent it from ending up in the JSON.
    private final transient Step step;
    private final LocalizedXStreams localizedXStreams;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, String featurePath, Step step, LocalizedXStreams localizedXStreams) {
        super(arguments, stepDefinition.getLocation(false));
        this.stepDefinition = stepDefinition;
        this.featurePath = featurePath;
        this.step = step;
        this.localizedXStreams = localizedXStreams;
    }

    public void runStep(I18n i18n) throws Throwable {
        try {
            stepDefinition.execute(i18n, transformedArgs(step, localizedXStreams.get(i18n.getLocale())));
        } catch (CucumberException e) {
            throw e;
        } catch (Throwable t) {
            throw removeFrameworkFramesAndAppendStepLocation(t, getStepLocation());
        }
    }

    /**
     * @param step    the step to run
     * @param xStream used to convert a string to declared stepdef arguments
     * @return an Array matching the types or {@code parameterTypes}, or an array of String if {@code parameterTypes} is null
     */
    private Object[] transformedArgs(Step step, LocalizedXStreams.LocalizedXStream xStream) {
        int argumentCount = getArguments().size();

        if (step.getRows() != null) {
            argumentCount++;
        } else if (step.getDocString() != null) {
            argumentCount++;
        }
        Integer parameterCount = stepDefinition.getParameterCount();
        if (parameterCount != null && argumentCount != parameterCount) {
            throw arityMismatch(parameterCount);
        }

        List<Object> result = new ArrayList<Object>();

        int n = 0;
        for (Argument a : getArguments()) {
            ParameterInfo parameterInfo = getParameterType(n, String.class);
            Object arg = parameterInfo.convert(a.getVal(), xStream);
            result.add(arg);
            n++;
        }

        if (step.getRows() != null) {
            result.add(tableArgument(step, n, xStream));
        } else if (step.getDocString() != null) {
            ParameterInfo parameterInfo = getParameterType(n, String.class);
            Object arg = parameterInfo.convert(step.getDocString().getValue(), xStream);
            result.add(arg);
        }
        return result.toArray(new Object[result.size()]);
    }

    private ParameterInfo getParameterType(int n, Type argumentType) {
        ParameterInfo parameterInfo = stepDefinition.getParameterType(n, argumentType);
        if (parameterInfo == null) {
            // Some backends return null because they don't know
            parameterInfo = new ParameterInfo(argumentType, null, null, false, null);
        }
        return parameterInfo;
    }

    private Object tableArgument(Step step, int argIndex, LocalizedXStreams.LocalizedXStream xStream) {
        ParameterInfo parameterInfo = getParameterType(argIndex, DataTable.class);
        TableConverter tableConverter = new TableConverter(xStream, parameterInfo);
        DataTable table = new DataTable(step.getRows(), tableConverter);
        Type type = parameterInfo.getType();
        return tableConverter.convert(table, type, parameterInfo.isTransposed());
    }

    private CucumberException arityMismatch(int parameterCount) {
        List<Argument> arguments = createArgumentsForErrorMessage(step);
        return new CucumberException(String.format(
                "Arity mismatch: Step Definition '%s' with pattern [%s] is declared with %s parameters. However, the gherkin step has %s arguments %s. \nStep: %s%s",
                stepDefinition.getLocation(true),
                stepDefinition.getPattern(),
                parameterCount,
                arguments.size(),
                arguments,
                step.getKeyword(),
                step.getName()
        ));
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

    private Throwable removeFrameworkFramesAndAppendStepLocation(Throwable error, StackTraceElement stepLocation) {
        StackTraceElement[] stackTraceElements = error.getStackTrace();
        if (stackTraceElements.length == 0 || stepLocation == null) {
            return error;
        }

        int newStackTraceLength;
        for (newStackTraceLength = 1; newStackTraceLength < stackTraceElements.length; ++newStackTraceLength) {
            if (stepDefinition.isDefinedAt(stackTraceElements[newStackTraceLength - 1])) {
                break;
            }
        }
        StackTraceElement[] newStackTrace = new StackTraceElement[newStackTraceLength + 1];
        System.arraycopy(stackTraceElements, 0, newStackTrace, 0, newStackTraceLength);
        newStackTrace[newStackTraceLength] = stepLocation;
        error.setStackTrace(newStackTrace);
        return error;
    }

    public String getPattern() {
        return stepDefinition.getPattern();
    }

    public StackTraceElement getStepLocation() {
        return step.getStackTraceElement(featurePath);
    }

    public String getStepName() {
        return step.getName();
    }
}
