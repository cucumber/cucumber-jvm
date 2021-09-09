package io.cucumber.guice;

import com.google.inject.Scope;
import org.apiguardian.api.API;

/**
 * A custom Guice scope that enables classes to be bound in a scope that will
 * last for the lifetime of one Cucumber scenario.
 */
@API(status = API.Status.STABLE)
public interface ScenarioScope extends Scope {

    void enterScope();

    void exitScope();

}
