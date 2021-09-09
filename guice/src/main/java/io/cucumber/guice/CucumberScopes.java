package io.cucumber.guice;

import com.google.inject.Module;
import org.apiguardian.api.API;

/**
 * Creates an instance of {@link ScenarioScope} for use when declaring bindings
 * in implementations of {@link Module}.
 * <p>
 * Note that when binding objects to the scenario scope it is recommended to
 * bind them to the {@link ScenarioScoped} annotation instead. E.g:
 * <code>bind(ScenarioScopedObject.class).in(ScenarioScoped.class);</code>
 */
@API(status = API.Status.STABLE)
public final class CucumberScopes {

    private CucumberScopes() {

    }

    /**
     * Creates a new instance of a ScenarioScope.
     *
     * @return a new instance of a ScenarioScope.
     */
    public static ScenarioScope createScenarioScope() {
        return new SequentialScenarioScope();
    }

}
