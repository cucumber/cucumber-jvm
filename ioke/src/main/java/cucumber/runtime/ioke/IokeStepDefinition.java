package cucumber.runtime.ioke;

import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import ioke.lang.IokeObject;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.lang.reflect.Type;
import java.util.List;

import static cucumber.runtime.Utils.listOf;

public class IokeStepDefinition implements StepDefinition {
    private final Runtime ioke;
    private final IokeObject iokeStepDefObject;
    private final IokeBackend backend;
    private final String location;
    private List<ParameterType> parameterTypes;

    public IokeStepDefinition(IokeBackend iokeBackend, Runtime ioke, IokeObject iokeStepDefObject, String location) throws Throwable {
        this.ioke = ioke;
        this.iokeStepDefObject = iokeStepDefObject;
        this.backend = iokeBackend;
        this.location = location;
        this.parameterTypes = getParameterTypes();
    }

    public String getPattern() {
        try {
            IokeObject regexp = (IokeObject) backend.invoke(iokeStepDefObject, "regexp_pattern");
            return regexp.toString();
        } catch (ControlFlow controlFlow) {
            throw new CucumberException("Couldn't get pattern", controlFlow);
        }
    }

    public List<Argument> matchedArguments(Step step) {
        try {
            Object args = backend.invoke(iokeStepDefObject, "arguments_from", step.getName());
            if (args.equals(ioke.nil)) {
                return null;
            } else {
                return (List<Argument>) args;
            }
        } catch (ControlFlow controlFlow) {
            throw new RuntimeException("Failed to get step args", controlFlow);
        }
    }

    public String getLocation(boolean detail) {
        return location;
    }

    @Override
    public Integer getParameterCount() {
        return parameterTypes.size();
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) {
        return parameterTypes.get(n);
    }

    public List<ParameterType> getParameterTypes() {
        try {
            IokeObject argNames = (IokeObject) backend.invoke(iokeStepDefObject, "arg_names");
            IokeObject argLength = (IokeObject) backend.invoke(argNames, "length");
            int groupCount = Integer.parseInt(argLength.toString()); // Not sure how to do this properly...

            return listOf(groupCount, new ParameterType(String.class, null, null));
        } catch (ControlFlow controlFlow) {
            throw new CucumberException("Couldn't inspect arity of stepdef", controlFlow);
        }
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
        backend.execute(iokeStepDefObject, args);
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName().equals(IokeBackend.class.getName());
    }
}
