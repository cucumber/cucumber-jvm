package io.cucumber.core.backend;

import io.cucumber.core.snippets.TestSnippet;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

public class StubBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
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
        public Snippet getSnippet() {
            return new TestSnippet();
        }

    }

}
