package cucumber.runtime;

import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.TypeRegistry;

import java.util.Collections;
import java.util.List;

/**
 * We need an implementation of Backend to prevent Runtime from blowing up.
 */
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
    public List<String> getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return Collections.singletonList("STUB SNIPPET");
    }
}
