package cucumber.runtime;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.runtime.table.TableConverter;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.ClassTransform;
import io.cucumber.cucumberexpressions.ConstructorTransform;
import io.cucumber.cucumberexpressions.Transform;
import io.cucumber.cucumberexpressions.TransformLookup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            List<Object> result = new ArrayList<Object>();
            for (Argument argument : getArguments()) {
                result.add(argument.getTransformedValue());
            }
            if (!step.getArgument().isEmpty()) {
                gherkin.pickles.Argument stepArgument = step.getArgument().get(0);
                TransformLookup transformLookup = new TransformLookup(Locale.ENGLISH);
                if (stepArgument instanceof PickleTable) {
                    result.add(tableArgument((PickleTable) stepArgument, result.size(), localizedXStreams.get(localeFor(language))));
                } else if (stepArgument instanceof PickleString) {
                    Type type = stepDefinition.getParameterType(result.size(), String.class).getType();
                    Transform<?> transform = null;
                    if (type != null) {
                        transform = transformLookup.lookupByType(type);
                    }
                    if (transform == null && type != null && type instanceof Class) {
                        transform = new ClassTransform((Class) type);
                    }
                    if (transform == null) {
                        transform = new ConstructorTransform(String.class);
                    }
                    if (transform == null) {

                    }
                    String stepArgumentContent = ((PickleString)stepArgument).getContent();
                    result.add(transform.transform(stepArgumentContent));
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
        TableConverter tableConverter = new TableConverter(xStream, parameterInfo);
        DataTable table = new DataTable(stepArgument, tableConverter);
        Type type = parameterInfo.getType();
        return tableConverter.convert(table, type, parameterInfo.isTransposed());
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
