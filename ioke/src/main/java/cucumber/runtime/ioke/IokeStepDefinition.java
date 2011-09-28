package cucumber.runtime.ioke;

import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import ioke.lang.IokeObject;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.lang.reflect.Type;
import java.util.List;

public class IokeStepDefinition implements StepDefinition {
    private final Runtime ioke;
    private final IokeObject iokeStepDefObject;
    private final IokeBackend backend;
    private final String location;

    public IokeStepDefinition(IokeBackend iokeBackend, Runtime ioke, IokeObject iokeStepDefObject, String location) throws Throwable {
        this.ioke = ioke;
        this.iokeStepDefObject = iokeStepDefObject;
        this.backend = iokeBackend;
        this.location = location;
    }

    public String getPattern() {
        try {
            return (String) backend.invoke(iokeStepDefObject, "regexp_source");
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

    @Override
    public Type getTypeForTableList(int argIndex) {
        return null;
    }

    public String getLocation() {
        return location;
    }

    public Class<?>[] getParameterTypes() {
        try {
            IokeObject argNames = (IokeObject) backend.invoke(iokeStepDefObject, "arg_names");
            IokeObject argLength = (IokeObject) backend.invoke(argNames, "length");
            int groupCount = Integer.parseInt(argLength.toString()); // Not sure how to do this properly...

            Class[] types = new Class[groupCount];
            for (int i = 0; i < types.length; i++) {
                types[i] = Object.class;
            }
            return types;
        } catch (ControlFlow controlFlow) {
            throw new CucumberException("Couldn't inspect arity of stepdef", controlFlow);
        }
    }

    public void execute(Object[] args) throws Throwable {
        backend.execute(iokeStepDefObject, args);
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName().equals(IokeBackend.class.getName());
    }
}
