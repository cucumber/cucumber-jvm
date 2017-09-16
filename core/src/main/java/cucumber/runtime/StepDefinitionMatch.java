package cucumber.runtime;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.TableConverter;
import cucumber.runtime.xstream.LocalizedXStreams;
import cucumber.util.Mapper;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static cucumber.util.FixJava.map;
import static java.util.Arrays.asList;

public class StepDefinitionMatch extends Match implements DefinitionMatch {
    private final StepDefinition stepDefinition;
    private final transient String featurePath;
    // The official JSON gherkin format doesn't have a step attribute, so we're marking this as transient
    // to prevent it from ending up in the JSON.
    private final transient PickleStep step;
    private final LocalizedXStreams localizedXStreams;

    public StepDefinitionMatch(List<Argument<?>> arguments, StepDefinition stepDefinition, String featurePath, PickleStep step, LocalizedXStreams localizedXStreams) {
        super(arguments, stepDefinition.getLocation(false));
        this.stepDefinition = stepDefinition;
        this.featurePath = featurePath;
        this.step = step;
        this.localizedXStreams = localizedXStreams;
    }

    @Override
    public void runStep(String language, Scenario scenario) throws Throwable {
        int argumentCount = getArguments().size();

        if (!step.getArgument().isEmpty()) {
            argumentCount++;
        }
        Integer parameterCount = stepDefinition.getParameterCount();
        if (parameterCount != null && argumentCount != parameterCount) {
            throw arityMismatch(parameterCount);
        }
        try {
            List<Object> result = new ArrayList<Object>();
            for (Argument argument : getArguments()) {
                result.add(argument.getValue());
            }
            if (!step.getArgument().isEmpty()) {
                gherkin.pickles.Argument stepArgument = step.getArgument().get(0);
                ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(Locale.ENGLISH);
                if (stepArgument instanceof PickleTable) {
                    result.add(tableArgument((PickleTable) stepArgument, result.size(), localizedXStreams.get(localeFor(language))));
                } else if (stepArgument instanceof PickleString) {
                    Type type = stepDefinition.getParameterType(result.size(), String.class).getType();
                    ParameterType<Object> transform = null;
                    if (type != null) {
                        String typeName = type.getTypeName();
                        if (typeName.startsWith("java.lang.")) {
                            typeName = typeName.substring("java.lang.".length()).toLowerCase();
                        }
                        transform = parameterTypeRegistry.lookupByTypeName(typeName);
                    }
                    if (transform == null) {
                        throw new IllegalArgumentException(type.getTypeName());
                    }
                    String stepArgumentContent = ((PickleString)stepArgument).getContent();
                    result.add(transform.transform(asList(stepArgumentContent)));
                }
            }
            stepDefinition.execute(language, result.toArray(new Object[result.size()]));
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
        TableConverter tableConverter = new cucumber.runtime.table.TableConverter(xStream, parameterInfo);
        DataTable table = new DataTable(stepArgument, tableConverter);
        Type type = parameterInfo.getType();
        return tableConverter.convert(table, type, parameterInfo.isTransposed());
    }

    private CucumberException arityMismatch(int parameterCount) {
        List<String> arguments = createArgumentsForErrorMessage(step);
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

    private List<String> createArgumentsForErrorMessage(PickleStep step) {
        List<String> arguments = new ArrayList<String>(getArguments().size());
        for (Argument argument : getArguments()) {
            arguments.add(argument.getValue().toString());
        }
        if (!step.getArgument().isEmpty()) {
            gherkin.pickles.Argument stepArgument = step.getArgument().get(0);
            if (stepArgument instanceof PickleString) {
                arguments.add("DocString:" + ((PickleString) stepArgument).getContent());
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
                arguments.add("Table:" + rows.toString());
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
