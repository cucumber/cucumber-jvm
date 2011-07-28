package cucumber.runtime.ioke;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import ioke.lang.IokeObject;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.util.List;
import java.util.Locale;

import cucumber.runtime.AbstractStepDefinition;
import cucumber.runtime.CucumberException;

public class IokeStepDefinition extends AbstractStepDefinition {
    private final Runtime ioke;
    private final IokeObject iokeStepDefObject;
    private final IokeBackend backend;
    private final String location;

    public IokeStepDefinition(IokeBackend iokeBackend, Runtime ioke, IokeObject iokeStepDefObject, String location, Locale locale) throws Throwable {
    	super(locale);
        this.ioke = ioke;
        this.iokeStepDefObject = iokeStepDefObject;
        this.backend = iokeBackend;
        this.location = location;
    }

    public String regexp_source() throws Throwable {
        return (String) backend.invoke(iokeStepDefObject, "regexp_source");
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
