package io.cucumber.core.runner;

import io.cucumber.core.snippets.SnippetType;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.options.RuntimeOptions;
import gherkin.pickles.PickleStep;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class TestRunnerSupplier implements Backend, RunnerSupplier, ObjectFactory {

    private final EventBus bus;
    private final RuntimeOptions runtimeOptions;

    protected TestRunnerSupplier(EventBus bus, RuntimeOptions runtimeOptions) {
        this.bus = bus;
        this.runtimeOptions = runtimeOptions;
    }


    @SuppressWarnings("unused") // Used by reflection
    public TestRunnerSupplier(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        this((EventBus) null, null);
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
    public List<String> getSnippet(PickleStep step, String keyword, SnippetType snippetType) {
        return emptyList();
    }

    @Override
    public Runner get() {
        return new Runner(bus, Collections.singleton(this), this, runtimeOptions);
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
