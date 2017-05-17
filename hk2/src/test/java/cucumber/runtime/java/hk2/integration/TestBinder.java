package cucumber.runtime.java.hk2.integration;

import cucumber.runtime.java.hk2.ScenarioScoped;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * Test Binder
 */
public class TestBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bindAsContract(ScenarioScopedObject.class).in(ScenarioScoped.class);
        bindAsContract(SingletonObject.class).in(Singleton.class);
        bindAsContract(UnScopedObject.class);
    }
}
