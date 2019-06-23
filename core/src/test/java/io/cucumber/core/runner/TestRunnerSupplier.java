package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.snippets.Snippet;
import io.cucumber.core.snippets.TestSnippet;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class TestRunnerSupplier implements Backend, RunnerSupplier, ObjectFactory {

    private final EventBus bus;
    private final RuntimeOptions runtimeOptions;
    private final TypeRegistry typeRegistry;

    protected TestRunnerSupplier(EventBus bus, TypeRegistry typeRegistry, RuntimeOptions runtimeOptions) {
        this.bus = bus;
        this.typeRegistry = typeRegistry;
        this.runtimeOptions = runtimeOptions;
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
    public Snippet getSnippet() {
        return new TestSnippet();
    }

    @Override
    public Runner get() {
        return new Runner(bus, Collections.singleton(this), this, typeRegistry, runtimeOptions);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        return false;
    }

    @Override
    public <T> T getInstance(Class<T> glueClass) {
        return null;
    }
}
