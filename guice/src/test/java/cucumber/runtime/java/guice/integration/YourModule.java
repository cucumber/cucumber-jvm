package cucumber.runtime.java.guice.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cucumber.api.guice.CucumberScopes;

public class YourModule extends AbstractModule {

    @Override
    protected void configure() {
        // UnScopedObject is implicitly bound without scope
        bind(ScenarioScopedObject.class).in(CucumberScopes.SCENARIO);
        bind(SingletonObject.class).in(Scopes.SINGLETON);
    }
}