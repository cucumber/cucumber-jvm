package cucumber.runtime;

import gherkin.formatter.model.Step;

import java.util.List;

public interface Backend {
    /**
     * Invoked before a new scenario starts. Implementations should do any necessary
     * setup of new, isolated state here.
     *
     * @param gluePaths where to load from
     * @param world     into where
     */
    void buildWorld(List<String> gluePaths, World world);

    void disposeWorld();

    String getSnippet(Step step);
}
