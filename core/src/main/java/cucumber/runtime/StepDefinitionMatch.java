package cucumber.runtime;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.runtime.table.TableConverter;
import cucumber.runtime.xstream.LocalizedXStreams;
import cucumber.util.Mapper;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static cucumber.util.FixJava.map;

public class StepDefinitionMatch extends Match implements DefinitionMatch {
    private final StepDefinition stepDefinition;
    private final transient String featurePath;
    // The official JSON gherkin format doesn't have a step attribute, so we're marking this as transient
    // to prevent it from ending up in the JSON.
    private final transient PickleStep step;
    private final LocalizedXStreams localizedXStreams;

    public StepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, String featurePath, PickleStep step, LocalizedXStreams localizedXStreams) {
        super(arguments, stepDefinition.getLocation(false));
        this.stepDefinition = stepDefinition;
        this.featurePath = featurePath;
        this.step = step;
        this.localizedXStreams = localizedXStreams;
    }

    @Override
    public void runStep(String language, Scenario scenario) throws Throwable {
        try {
            stepDefinition.execute(language, transformedArgs(step, localizedXStreams.get(localeFor(language))));
        } catch (CucumberException e) {
            throw e;
        } catch (Throwable t) {
            throw removeFrameworkFramesAndAppendStepLocation(t, getStepLocation());
        }
    }

    @Override
    public void dryRunStep(String language, Scenario scenario) throws Throwable {
        // Do nothing
    }

    /**
     * @param step    the step to run
     * @param xStream used to convert a string to declared stepdef arguments
     * @return an Array matching the types or {@code parameterTypes}, or an array of String if {@code parameterTypes} is null
     */
    private Object[] transformedArgs(PickleStep step, LocalizedXStreams.LocalizedXStream xStream) {
        int argumentCount = getArguments().size();

        if (!step.getArgument().isEmpty()) {
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

        if (!step.getArgument().isEmpty()) {
            gherkin.pickles.Argument stepArgument = step.getArgument().get(0);
            if (stepArgument instanceof PickleTable) {
                result.add(tableArgument((PickleTable) stepArgument, n, xStream));
            } else if (stepArgument instanceof PickleString) {
                ParameterInfo parameterInfo = getParameterType(n, String.class);
                Object arg = parameterInfo.convert(((PickleString) stepArgument).getContent(), xStream);
                result.add(arg);
            }
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

    private Object tableArgument(PickleTable stepArgument, int argIndex, LocalizedXStreams.LocalizedXStream xStream) {
        ParameterInfo parameterInfo = getParameterType(argIndex, DataTable.class);
        TableConverter tableConverter = new TableConverter(xStream, parameterInfo);
        DataTable table = new DataTable(stepArgument, tableConverter);
        Type type = parameterInfo.getType();
        return tableConverter.convert(table, type, parameterInfo.isTransposed());
    }

    private CucumberException arityMismatch(int parameterCount) {
        List<Argument> arguments = createArgumentsForErrorMessage(step);
        return new CucumberException(String.format(
                "Arity mismatch: Step Definition '%s' with pattern [%s] is declared with %s parameters. However, the gherkin step has %s arguments %s. \nStep text: %s",
                stepDefinition.getLocation(true),
                stepDefinition.getPattern(),
                parameterCount,
                arguments.size(),
                arguments,
                step.getText()
        ));
    }

    private List<Argument> createArgumentsForErrorMessage(PickleStep step) {
        List<Argument> arguments = new ArrayList<Argument>(getArguments());
        if (!step.getArgument().isEmpty()) {
            gherkin.pickles.Argument stepArgument = step.getArgument().get(0);
            if (stepArgument instanceof PickleString) {
                arguments.add(new Argument(-1, "DocString:" + ((PickleString) stepArgument).getContent()));
            } else if (stepArgument instanceof PickleTable) {
                List<List<String>> rows = map(((PickleTable) stepArgument).getRows(), new Mapper<PickleRow, List<String>>() {
                    @Override
                    public List<String> map(PickleRow row) {
                        List<String> raw = new ArrayList<String>(row.getCells().size());
                        for (PickleCell pickleCell : row.getCells()) {
                            raw.add(pickleCell.getValue());
                        }
                        return raw;
                    }
                });
                arguments.add(new Argument(-1, "Table:" + rows.toString()));
            }
        }
        return arguments;
    }

    protected Throwable removeFrameworkFramesAndAppendStepLocation(Throwable error, StackTraceElement stepLocation) {
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

    private Locale localeFor(String language) {
        String[] languageAndCountry = language.split("-");
        if (languageAndCountry.length == 1) {
            return new Locale(language);
        } else {
            return new Locale(languageAndCountry[0], languageAndCountry[1]);
        }
    }

    @Override
    public String getPattern() {
        return stepDefinition.getPattern();
    }

    public StackTraceElement getStepLocation() {
        return new StackTraceElement("✽", step.getText(), featurePath, getStepLine(step));
    }

    public Match getMatch() {
        return this;
    }

    @Override
    public String getCodeLocation() {
        return stepDefinition.getLocation(false);
    }

    public static int getStepLine(PickleStep step) {
        return step.getLocations().get(step.getLocations().size() - 1).getLine();
    }
}
