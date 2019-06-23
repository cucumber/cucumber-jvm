package io.cucumber.core.api.event;

public interface StepDefinition {

    /**
     * The source line where the step definition is defined.
     * Example: foo/bar/Zap.brainfuck:42
     *
     * @param detail true if extra detailed location information should be included.
     * @return The source line of the step definition.
     */
    String getLocation(boolean detail);

    /**
     * @return How many declared parameters this step definition has. Returns null if unknown.
     */
    Integer getParameterCount();

    /**
     * @return the pattern associated with this instance. Used for error reporting only.
     */
    String getPattern();
}
