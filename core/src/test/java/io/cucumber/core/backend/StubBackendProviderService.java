package io.cucumber.core.backend;

import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.snippets.Snippet;
import io.cucumber.core.snippets.TestSnippet;

import java.net.URI;
import java.util.List;

public class StubBackendProviderService implements BackendProviderService {
    @Override
    public Backend create(Lookup lookup, Container container, ResourceLoader resourceLoader) {
        return new StubBackend();
    }

    static class StubBackend implements Backend {

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
        public Snippet getSnippet(){
            return new TestSnippet();
        }

    }

}
