package io.cucumber.junit;


import gherkin.pickles.PickleStep;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.net.URI;
import java.util.List;

import static java.util.Collections.singletonList;

public class StubBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Container container, ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        return new StubBackend();
    }

    /**
     * We need an implementation of Backend to prevent Runtime from blowing up.
     */
    public static class StubBackend implements Backend {
        StubBackend() {

        }

        @Override
        public void loadGlue(Glue glue, List<URI> gluePaths) {
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

}
