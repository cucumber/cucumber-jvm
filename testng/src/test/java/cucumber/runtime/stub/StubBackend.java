package cucumber.runtime.stub;

import io.cucumber.stepexpression.TypeRegistry;
import cucumber.runtime.Backend;
import cucumber.runtime.Glue;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

import java.util.List;

/**
 * We need an implementation of Backend to prevent Runtime from blowing up.
 */
@SuppressWarnings("unused")
public class StubBackend implements Backend {
    public StubBackend(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {

    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
    }

    @Override
    public void buildWorld() {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return "STUB SNIPPET";
    }
}
