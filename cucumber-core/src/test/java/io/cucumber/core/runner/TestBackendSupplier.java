package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.snippets.TestSnippet;

import java.util.Collection;
import java.util.Collections;

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
        return Collections.singleton(this);
    }

}
