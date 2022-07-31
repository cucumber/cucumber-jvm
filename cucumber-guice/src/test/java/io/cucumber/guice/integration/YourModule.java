package io.cucumber.guice.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.cucumber.guice.ScenarioScoped;

public class YourModule extends AbstractModule {

    @Override
    protected void configure() {
        // UnScopedObject is implicitly bound without scope
        bind(ScenarioScopedObject.class).in(ScenarioScoped.class);
        bind(SingletonObject.class).in(Scopes.SINGLETON);
    }

}
