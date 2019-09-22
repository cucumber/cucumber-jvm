package io.cucumber.guice;

import com.google.inject.Module;
import org.apiguardian.api.API;

/**
 * Provides a convenient {@link Module} instance that contains bindings for
 * {@link ScenarioScoped} annotation and for {@link ScenarioScope}.
 */
@API(status = API.Status.STABLE)
public final class CucumberModules {

    private CucumberModules(){

    }

    /**
     * A convenient instance of {@link Module}. Should only be used
     * in combination with {@link CucumberScopes#SCENARIO}.
     * <p>
     * Note that using this in combination with parallel execution results in
     * undefined behaviour.
     *
     * @deprecated please use {@link #createScenarioModule()} instead
     */
    @Deprecated
    public static final Module SCENARIO = createScenarioModule();

    public static Module createScenarioModule() {
        return new ScenarioModule(CucumberScopes.createScenarioScope());
    }

    public static Module createScenarioModule(ScenarioScope scenarioScope) {
        return new ScenarioModule(scenarioScope);
    }
}
