package cucumber.runtime;

import gherkin.formatter.model.Step;

import java.util.List;

public interface Backend {
    /**
     * Invoked once before all features. This is where stepdefs and hooks should be loaded.
     */
    void loadGlue(Glue glue, List<String> gluePaths);

    /**
     * Invoked before a new scenario starts. Implementations should do any necessary
     * setup of new, isolated state here.
     */
    void buildWorld();

    void disposeWorld();

    String getSnippet(Step step);
}
