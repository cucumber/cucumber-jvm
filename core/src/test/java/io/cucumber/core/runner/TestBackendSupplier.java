package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.snippets.TestSnippet;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class TestBackendSupplier implements Backend, BackendSupplier {

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


    @Override
    public Collection<? extends Backend> get() {
        return  Collections.singleton(this);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {

    }
}
