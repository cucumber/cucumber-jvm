package cucumber.runtime;

import gherkin.formatter.model.Step;

import java.util.List;

public interface Backend {
    /**
     * Invoked before a new scenario starts. Implementations should do any necessary
     * setup of new, isolated state here.
     *
     * @param codePaths
     * @param world
     */
    void buildWorld(List<String> codePaths, World world);

    void disposeWorld();

    String getSnippet(Step step);
}
