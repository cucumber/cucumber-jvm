package io.cucumber.core.backend;

import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.io.ResourceLoader;
import gherkin.pickles.PickleStep;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.List;

import static java.util.Collections.emptyList;

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
    public List<String> getSnippet(PickleStep step, String keyword, SnippetType.FunctionNameGenerator functionNameGenerator) {
        return emptyList();
    }
}
