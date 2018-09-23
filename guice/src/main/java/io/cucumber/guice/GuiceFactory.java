package io.cucumber.guice;

import com.google.inject.Injector;
import io.cucumber.java.api.ObjectFactory;
import io.cucumber.core.options.Env;
import io.cucumber.guice.api.ScenarioScope;

/**
 * Guice implementation of the <code>io.cucumber.java.api.ObjectFactory</code>.
 */
public class GuiceFactory implements ObjectFactory {

    private final Injector injector;

    public GuiceFactory() {
        this(new InjectorSourceFactory(Env.INSTANCE).create().getInjector());
    }

    /**
     * Package private constructor that is called by the public constructor at runtime and is also called directly by
     * tests.
     *
     * @param injector an injector configured with a binding for <code>ScenarioScope</code>.
     */
    GuiceFactory(Injector injector) {
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
