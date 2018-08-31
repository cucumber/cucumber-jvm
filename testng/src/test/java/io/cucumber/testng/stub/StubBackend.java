package io.cucumber.runtime.stub;

import cucumber.api.SnippetType;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.io.ResourceLoader;
import gherkin.pickles.PickleStep;

import java.util.List;

import static java.util.Collections.singletonList;

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
    public List<String> getSnippet(PickleStep step, String keyword, SnippetType.FunctionNameGenerator functionNameGenerator) {
        return singletonList("STUB SNIPPET");
    }
}
