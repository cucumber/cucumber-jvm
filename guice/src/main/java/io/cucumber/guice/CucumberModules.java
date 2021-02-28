package io.cucumber.guice;

import com.google.inject.Module;
import org.apiguardian.api.API;

/**
 * Provides a convenient {@link Module} instance that contains bindings for
 * {@link ScenarioScoped} annotation and for {@link ScenarioScope}.
 */
@API(status = API.Status.STABLE)
public final class CucumberModules {

    private CucumberModules() {

    }

    public static Module createScenarioModule() {
        return new ScenarioModule(CucumberScopes.createScenarioScope());
    }

    public static Module createScenarioModule(ScenarioScope scenarioScope) {
        return new ScenarioModule(scenarioScope);
    }

}
