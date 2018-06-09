package cucumber.runtime;

import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.TypeRegistry;

import java.util.List;

public class StubBackend implements Backend {

    @SuppressWarnings("unused") // reflection to create backend
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
        return null;
    }
}
