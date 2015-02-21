package cucumber.runtime;

public interface Hook {
    /**
     * The source line where the step definition is defined.
     * Example: foo/bar/Zap.brainfuck:42
     *
     * @param detail true if extra detailed location information should be included.
     */
    String getLocation(boolean detail);

    int getOrder();

    /**
     * @return true if this instance is scoped to a single scenario, or false if it can be reused across scenarios.
     */
    boolean isScenarioScoped();
}