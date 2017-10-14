package cucumber.runtime.java.guice.impl;

import com.google.inject.Injector;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Env;
import cucumber.runtime.java.guice.ScenarioScope;

/**
 * Guice implementation of the <code>cucumber.api.java.ObjectFactory</code>.
 */
public class GuiceFactory implements ObjectFactory {

    private final Injector injector;

    public GuiceFactory() {
        this(new InjectorSourceFactory(Env.INSTANCE).create().getInjector());
    }

    /** Construct a GuiceFactory from an existing Injector.
     *
     * @param injector an injector configured with a binding for <code>cucumber.runtime.java.guice.ScenarioScope</code>.
     */
    public GuiceFactory(Injector injector) {
        this.injector = injector;
    }

    public boolean addClass(Class<?> clazz) {
        return true;
    }

    public void start() {
        injector.getInstance(ScenarioScope.class).enterScope();
    }

    public void stop() {
        injector.getInstance(ScenarioScope.class).exitScope();
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

}
