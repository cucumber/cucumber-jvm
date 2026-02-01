package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.snippets.TestSnippet;

import java.util.Collection;
import java.util.Collections;

public abstract class TestBackendSupplier implements Backend, BackendSupplier {

    @Override
    public final Snippet getSnippet() {
        return new TestSnippet();
    }

    @Override
    public final Collection<? extends Backend> get() {
        return Collections.singleton(this);
    }

}
