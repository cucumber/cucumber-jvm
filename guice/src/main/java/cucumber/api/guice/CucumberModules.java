package cucumber.api.guice;

import com.google.inject.Module;
import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.impl.ScenarioModule;

/**
 * Provides a convenient <code>com.google.inject.Module</code> instance that contains bindings for
 * <code>cucumber.runtime.java.guice.ScenarioScoped</code> annotation and for
 * <code>cucumber.runtime.java.guice.ScenarioScope</code>.
 */
public class CucumberModules {
    /**
     * @deprecated please use {@link #createScenarioModule()} instead
     */
    @Deprecated
    public static final Module SCENARIO = new ScenarioModule(CucumberScopes.SCENARIO);

    public static Module createScenarioModule() {
        return new ScenarioModule(CucumberScopes.createScenario());
    }

    public static Module createScenarioModule(ScenarioScope scenarioScope) {
        return new ScenarioModule(scenarioScope);
    }
}
