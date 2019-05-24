package cucumber.runtime;

import io.cucumber.stepexpression.Argument;
import gherkin.pickles.PickleStep;

import java.util.List;

public interface StepDefinition {
    /**
     * Returns a list of arguments. Return null if the step definition
     * doesn't match at all. Return an empty List if it matches with 0 arguments
     * and bigger sizes if it matches several.
     */
    List<Argument> matchedArguments(PickleStep step);

    /**
     * The source line where the step definition is defined.
     * Example: foo/bar/Zap.brainfuck:42
     *
     * @param detail true if extra detailed location information should be included.
     */
    String getLocation(boolean detail);

    /**
     * How many declared parameters this step definition has. Returns null if unknown.
     */
    Integer getParameterCount();

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
     * @return the pattern associated with this instance. Used for error reporting only.
     */
    String getPattern();

    /**
     * @deprecated replaced with {@link ScenarioScoped}
     * @return true if this instance is scoped to a single scenario, or false if it can be reused across scenarios.
     */
    @Deprecated
    boolean isScenarioScoped();
}
