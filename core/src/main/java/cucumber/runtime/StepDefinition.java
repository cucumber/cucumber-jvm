package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.List;
import java.util.Locale;

public interface StepDefinition {
    /**
     * Returns a list of arguments. Return null if the step definition
     * doesn't match at all. Return an empty List if it matches with 0 arguments
     * and bigger sizes if it matches several.
     */
    List<Argument> matchedArguments(Step step);

    /**
     * The source line where the step definition is defined.
     * Example: foo/bar/Zap.brainfuck:42
     */
    String getLocation();

    /**
     * The parameter types this step definition can be invoked with.
     * This will be used to coerce string values from arguments before
     * invoking the step definition. The size of the returned array
     * must be equal to the number of arguments accepted by execute.
     * <p/>
     * If the parameter types are unknown at runtime, the result may be null.
     */
    Class<?>[] getParameterTypes();

    /**
     * Invokes the step definition. The method should raise a Throwable
     * if the invocation fails, which will cause the step to fail.
     */
    void execute(Object[] args) throws Throwable;

    /**
     * Return true if this matches the location. This is used to filter
     * stack traces.
     */
    boolean isDefinedAt(StackTraceElement stackTraceElement); // TODO: redundant with getLocation?

    /**
     * @return the locale associated with the step
     */
    Locale getLocale();
}
