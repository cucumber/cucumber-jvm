package cucumber.api.guice;

import com.google.inject.Module;
import cucumber.runtime.java.guice.impl.ScenarioModule;

/**
 * Provides a convenient <code>com.google.inject.Module</code> instance that contains bindings for
 * <code>cucumber.runtime.java.guice.ScenarioScoped</code> annotation and for
 * <code>cucumber.runtime.java.guice.ScenarioScope</code>.
 */
public class CucumberModules {
    public static final Module SCENARIO = new ScenarioModule(CucumberScopes.SCENARIO);
}
