package cucumber.runtime.clojure;

import clojure.lang.AFunction;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import cucumber.table.Table;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class ClojureStepDefinition implements StepDefinition {
    private final Pattern pattern;
    private final AFunction closure;
    private StackTraceElement location;

    public ClojureStepDefinition(Pattern pattern, AFunction closure, StackTraceElement location) {
        this.pattern = pattern;
        this.closure = closure;
        this.location = location;
    }

    // Clojure's AFunction.invokeWithArgs doesn't take varargs :-/
    private Method lookupInvokeMethod(Object[] args) throws NoSuchMethodException {
        return AFunction.class.getMethod("invoke", Utils.classArray(args.length, Object.class));
    }

    public List<Argument> matchedArguments(Step step) {
        return new JdkPatternArgumentMatcher(pattern).argumentsFrom(step.getName());
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

    @Override
    public String getPattern() {
        return pattern.pattern();
    }

    @Override
    public Object tableArgument(int argIndex, Table table) {
        return table;
    }
}
