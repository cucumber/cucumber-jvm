package cucumber.runtime;

import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

import java.util.List;

public interface Backend {
    /**
     * Invoked once before all features. This is where stepdefs and hooks should be loaded.
     */
    void loadGlue(Glue glue, List<String> gluePaths);

    /**
     * Invoked once, handing the backend a reference to a step executor
     * in case the backend needs to call steps defined within other steps
     */
    void setUnreportedStepExecutor(UnreportedStepExecutor executor);

    /**
     * Invoked before a new scenario starts. Implementations should do any necessary
     * setup of new, isolated state here.
     */
    void buildWorld(Glue glue);

    /**
     * Invoked at the end of a scenario, after hooks
     */
    void disposeWorld(Glue glue);

    String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator);
}
