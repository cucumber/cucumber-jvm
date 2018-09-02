package io.cucumber.guice.impl;

import com.google.inject.AbstractModule;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.guice.ScenarioScope;

public class ScenarioModule extends AbstractModule {

    private final ScenarioScope scenarioScope;

    public ScenarioModule(ScenarioScope scenarioScope) {
        this.scenarioScope = scenarioScope;
    }

    /**
     * Configures a {@link com.google.inject.Binder} via the exposed methods.
     */
    @Override
    protected void configure() {
        bindScope(ScenarioScoped.class, scenarioScope);
        bind(ScenarioScope.class).toInstance(scenarioScope);
    }
}
