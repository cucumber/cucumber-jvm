package cucumber.runtime;

import gherkin.formatter.model.Tag;

import java.util.Collection;

public interface HookDefinition<T> {
    /**
     * The source line where the step definition is defined.
     * Example: foo/bar/Zap.brainfuck:42
     *
     * @param detail true if extra detailed location information should be included.
     */
    String getLocation(boolean detail);

    /**
     * @return order in which this hook should be run.
     */
    int getOrder();

    /**
     * @param tags collection of the tag to run search against.
     * @return if hook has on of the tags.
     */
    boolean matches(Collection<Tag> tags);

    /**
     * @return true if this instance is scoped to a single scenario, or false if it can be reused across scenarios.
     */
    boolean isScenarioScoped();

    void execute(T type) throws Throwable;
}