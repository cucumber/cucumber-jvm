package io.cucumber.core.backend;

import io.cucumber.core.api.Scenario;
import gherkin.pickles.PickleTag;
import org.apiguardian.api.API;

import java.util.Collection;

@API(status = API.Status.STABLE)
public interface HookDefinition {
    /**
     * The source line where the step definition is defined.
     * Example: foo/bar/Zap.brainfuck:42
     *
     * @param detail true if extra detailed location information should be included.
     * @return The source line where the step definition is defined.
     */
    String getLocation(boolean detail);

    void execute(Scenario scenario) throws Throwable;

    boolean matches(Collection<PickleTag> tags);

    int getOrder();
}
