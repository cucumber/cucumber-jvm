package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.snippets.TestSnippet;

import java.net.URI;
import java.util.List;

import static java.util.Collections.singleton;

public class TestRunnerSupplier implements Backend, RunnerSupplier, ObjectFactory {

    private final EventBus bus;
    private final RuntimeOptions runtimeOptions;

    protected TestRunnerSupplier(EventBus bus, RuntimeOptions runtimeOptions) {
        this.bus = bus;
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
        return new Runner(bus, singleton(this), this, typeRegistry -> {
        }, runtimeOptions);
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
