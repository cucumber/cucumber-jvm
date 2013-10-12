package cucumber.runtime.stub;

import cucumber.runtime.Backend;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.formatter.model.Step;

import java.util.List;

/**
 * We need an implementation of Backend to prevent Runtime from blowing up.
 */
public class StubBackend implements Backend {
    public StubBackend(ResourceLoader resourceLoader) {

    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
    }

    @Override
    public void buildWorld() {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return "STUB SNIPPET";
    }
}
