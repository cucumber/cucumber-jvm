package cucumber.runtime;

import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Type;
import java.util.List;

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
     *
     * @param detail true if extra detailed location information should be included.
     */
    String getLocation(boolean detail);

    /**
     * How many declared parameters this stepdefinition has. Returns null if unknown.
     */
    Integer getParameterCount();

    /**
     * The parameter type at index n. A hint about the raw parameter type is passed to make
     * it easier for the implementation to make a guess based on runtime information.
     *
     * Statically typed languages will typically ignore the {@code argumentType} while dynamically
     * typed ones will use it to infer a "good type". It's also ok to return null.
     */
    ParameterType getParameterType(int n, Type argumentType) throws IndexOutOfBoundsException;

    /**
     * Invokes the step definition. The method should raise a Throwable
     * if the invocation fails, which will cause the step to fail.
     */
    void execute(I18n i18n, Object[] args) throws Throwable;

    /**
     * Return true if this matches the location. This is used to filter
     * stack traces.
     */
    boolean isDefinedAt(StackTraceElement stackTraceElement); // TODO: redundant with getLocation?

    /**
     * @return the pattern associated with this instance. Used for error reporting only.
     */
    String getPattern();
}
