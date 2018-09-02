package cucumber.api.guice;

import com.google.inject.Module;
import io.cucumber.guice.impl.ScenarioModule;

/**
 * Provides a convenient <code>com.google.inject.Module</code> instance that contains bindings for
 * <code>ScenarioScoped</code> annotation and for
 * <code>ScenarioScope</code>.
 */
public class CucumberModules {
    public static final Module SCENARIO = new ScenarioModule(CucumberScopes.SCENARIO);
}
