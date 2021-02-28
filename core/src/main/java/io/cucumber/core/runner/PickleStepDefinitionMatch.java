package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.cucumberexpressions.CucumberExpressionException;
import io.cucumber.datatable.CucumberDataTableException;
import io.cucumber.datatable.UndefinedDataTableTypeException;
import io.cucumber.docstring.CucumberDocStringException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.cucumber.core.runner.StackManipulation.removeFrameworkFrames;
import static io.cucumber.core.runner.StackManipulation.removeFrameworkFramesAndAppendStepLocation;

class PickleStepDefinitionMatch extends Match implements StepDefinitionMatch {

    private final StepDefinition stepDefinition;
    private final URI uri;
    private final Step step;

    PickleStepDefinitionMatch(List<Argument> arguments, StepDefinition stepDefinition, URI uri, Step step) {
        super(arguments, stepDefinition.getLocation());
        this.stepDefinition = stepDefinition;
        this.uri = uri;
        this.step = step;
    }

    @Override
    public void runStep(TestCaseState state) throws Throwable {
        List<Argument> arguments = getArguments();
        List<ParameterInfo> parameterInfos = stepDefinition.parameterInfos();
        if (parameterInfos != null && arguments.size() != parameterInfos.size()) {
            throw arityMismatch(parameterInfos.size());
        }
        List<Object> result = new ArrayList<>();
        try {
            for (Argument argument : arguments) {
                result.add(argument.getValue());
            }
        } catch (UndefinedDataTableTypeException e) {
            throw registerDataTableTypeInConfiguration(e);
        } catch (CucumberExpressionException | CucumberDataTableException | CucumberDocStringException e) {
            CucumberInvocationTargetException targetException;
            if ((targetException = causedByCucumberInvocationTargetException(e)) != null) {
                throw removeFrameworkFrames(targetException);
            }
            throw couldNotConvertArguments(e);
        } catch (CucumberBackendException e) {
            throw couldNotInvokeArgumentConversion(e);
        } catch (CucumberInvocationTargetException e) {
            throw removeFrameworkFrames(e);
        }
        try {
            stepDefinition.execute(result.toArray(new Object[0]));
        } catch (CucumberBackendException e) {
            throw couldNotInvokeStep(e, result);
        } catch (CucumberInvocationTargetException e) {
            throw removeFrameworkFramesAndAppendStepLocation(e, getStepLocation());
        }
    }

    @Override
    public void dryRunStep(TestCaseState state) throws Throwable {
        // Do nothing
    }

    @Override
    public String getCodeLocation() {
        return stepDefinition.getLocation();
    }

    private CucumberException arityMismatch(int parameterCount) {
        List<String> arguments = createArgumentsForErrorMessage();
        return new CucumberException(String.format(
            "Step [%s] is defined with %s parameters at '%s'.\n" +
                    "However, the gherkin step has %s arguments%sStep text: %s",
            stepDefinition.getPattern(),
            parameterCount,
            stepDefinition.getLocation(),
            arguments.size(),
            formatArguments(arguments),
            step.getText()));
    }

    private CucumberException registerDataTableTypeInConfiguration(Exception e) {
        // TODO: Add doc URL
        return new CucumberException(String.format("" +
                "Could not convert arguments for step [%s] defined at '%s'.\n" +
                "It appears you did not register a data table type.",
            stepDefinition.getPattern(),
            stepDefinition.getLocation()), e);
    }

    private CucumberInvocationTargetException causedByCucumberInvocationTargetException(RuntimeException e) {
        Throwable cause = e.getCause();
        if (cause instanceof CucumberInvocationTargetException) {
            return (CucumberInvocationTargetException) cause;
        }
        return null;
    }

    private CucumberException couldNotConvertArguments(Exception e) {
        return new CucumberException(String.format(
            "Could not convert arguments for step [%s] defined at '%s'.",
            stepDefinition.getPattern(),
            stepDefinition.getLocation()), e);
    }

    private CucumberException couldNotInvokeArgumentConversion(CucumberBackendException e) {
        // TODO: Add doc URL
        return new CucumberException(String.format("" +
                "Could not convert arguments for step [%s] defined at '%s'.\n" +
                "It appears there was a problem with a hook or transformer definition.",
            stepDefinition.getPattern(),
            stepDefinition.getLocation()), e);
    }

    private Throwable couldNotInvokeStep(CucumberBackendException e, List<Object> result) {
        String argumentTypes = createArgumentTypes(result);
        // TODO: Add doc URL
        return new CucumberException(String.format("" +
                "Could not invoke step [%s] defined at '%s'.\n" +
                "It appears there was a problem with the step definition.\n" +
                "The converted arguments types were (" + argumentTypes + ")",
            stepDefinition.getPattern(),
            stepDefinition.getLocation()), e);
    }

    private StackTraceElement getStepLocation() {
        return new StackTraceElement("✽", step.getText(), uri.toString(), step.getLine());
    }

    private List<String> createArgumentsForErrorMessage() {
        List<String> arguments = new ArrayList<>(getArguments().size());
        for (Argument argument : getArguments()) {
            arguments.add(argument.toString());
        }
        return arguments;
    }

    private String formatArguments(List<String> arguments) {
        if (arguments.isEmpty()) {
            return ".\n";
        }

        StringBuilder formatted = new StringBuilder(":\n");
        for (String argument : arguments) {
            formatted.append(" * ").append(argument).append("\n");
        }
        return formatted.toString();
    }

    private String createArgumentTypes(List<Object> result) {
        return result.stream()
                .map(o -> o == null ? "null" : o.getClass().getName())
                .collect(Collectors.joining(", "));
    }

    public String getPattern() {
        return stepDefinition.getPattern();
    }

    StepDefinition getStepDefinition() {
        return stepDefinition;
    }

}
