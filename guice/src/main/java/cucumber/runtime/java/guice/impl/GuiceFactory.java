package cucumber.runtime.java.guice.impl;

import com.google.inject.Injector;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Env;
import cucumber.runtime.java.guice.ScenarioScope;

/**
 * Guice implementation of the <code>cucumber.api.java.ObjectFactory</code>.
 */
public class GuiceFactory implements ObjectFactory {

    private final ThreadLocal<Injector> injector = new ThreadLocal<Injector>();

    public GuiceFactory() {
    }

    /**
     * Package private constructor that is called by the public constructor at runtime and is also called directly by
     * tests.
     *
     * @param injector an injector configured with a binding for <code>cucumber.runtime.java.guice.ScenarioScope</code>.
     */
    GuiceFactory(Injector injector) {
        this.injector.set(injector);
    }

    public boolean addClass(Class<?> clazz) {
        return true;
    }

    public void start() {
        if (injector.get() == null) {
            injector.set(new InjectorSourceFactory(Env.INSTANCE).create().getInjector());
            
        }
        injector.get().getInstance(ScenarioScope.class).enterScope();
    }

    public void stop() {
        injector.get().getInstance(ScenarioScope.class).exitScope();
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.get().getInstance(clazz);
    }

}
