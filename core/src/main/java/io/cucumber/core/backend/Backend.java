package io.cucumber.core.backend;

import io.cucumber.core.api.options.SnippetType;
import gherkin.pickles.PickleStep;

import java.net.URI;
import java.util.List;

public interface Backend {
    /**
     * Invoked once before all features. This is where stepdefs and hooks should be loaded.
     * 
     * @param glue Glue that provides the stepdefs to be executed.
     * @param gluePaths The locations for the glue to be loaded.
     */
    void loadGlue(Glue glue, List<URI> gluePaths);

    /**
     * Invoked before a new scenario starts. Implementations should do any necessary
     * setup of new, isolated state here.
     */
    void buildWorld();

    /**
     * Invoked at the end of a scenario, after hooks
     */
    void disposeWorld();

    List<String> getSnippet(PickleStep step, String keyword, SnippetType.FunctionNameGenerator functionNameGenerator);
}
