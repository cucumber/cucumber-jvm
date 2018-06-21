package cucumber.runtime;

import cucumber.messages.Pickles.PickleStep;
import cucumber.runtime.snippets.FunctionNameGenerator;

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

    /**
     * Invoked at the end of a scenario, after hooks
     */
    void disposeWorld();

    String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator);
}
