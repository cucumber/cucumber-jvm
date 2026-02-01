package io.cucumber.core.backend;

import io.cucumber.core.snippets.TestSnippet;

import java.util.function.Supplier;

public final class StubBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        return new StubBackend();
    }

    static class StubBackend implements Backend {

        @Override
        public Snippet getSnippet() {
            return new TestSnippet();
        }

    }

}
