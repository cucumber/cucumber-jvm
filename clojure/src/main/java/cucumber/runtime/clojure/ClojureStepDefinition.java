package cucumber.runtime.clojure;

import cucumber.runtime.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import clojure.lang.AFunction;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.Utils;

public class ClojureStepDefinition implements StepDefinition {
    private final Pattern regexp;
    private final AFunction closure;
    private StackTraceElement location;

    public ClojureStepDefinition(Pattern regexp, AFunction closure, StackTraceElement location) {
    	this.regexp = regexp;
        this.closure = closure;
        this.location = location;
    }

    // Clojure's AFunction.invokeWithArgs doesn't take varargs :-/
    private Method lookupInvokeMethod(Object[] args) throws NoSuchMethodException {
        return AFunction.class.getMethod("invoke", Utils.objectClassArray(args.length));
    }

    public List<Argument> matchedArguments(Step step) {
        return new JdkPatternArgumentMatcher(regexp).argumentsFrom(step.getName());
    }

    public String getLocation() {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    public Class<?>[] getParameterTypes() {
        return null;
    }

    public void execute(Object[] args) throws Throwable {
        Method functionInvoke = lookupInvokeMethod(args);
        try {
            functionInvoke.invoke(closure, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }
}
