package io.cucumber.core.backend;

import gherkin.pickles.PickleStep;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.List;

import static java.util.Collections.emptyList;

public class StubBackendProviderService implements BackendProviderService {
    @Override
    public Backend create(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        return new StubBackend();
    }

    static class StubBackend implements Backend {

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
}
