package cucumber.runtime.java.guice.impl;

import com.google.inject.Injector;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.guice.ScenarioScope;

import java.io.IOException;

/**
 * Guice implementation of the <code>cucumber.runtime.java.ObjectFactory</code>.
 */
public class GuiceFactory implements ObjectFactory {

    private final Injector injector;

    /**
     * This constructor is called reflectively by cucumber.runtime.Refections.
     * @throws IOException if a <code>cucumber-guice.properties</code> file is in the root of the classpath and it
     * cannot be loaded.
     */
    public GuiceFactory() throws IOException {
        this(new InjectorSourceFactory(PropertiesLoader.loadGuiceProperties()).create().getInjector());
    }

    /**
     * Package private constructor that is called by the public constructor at runtime and is also called directly by
     * tests.
     * @param injector an injector configured with a binding for <code>cucumber.runtime.java.guice.ScenarioScope</code>.
     */
    GuiceFactory(Injector injector) {
        this.injector = injector;
    }

    public void addClass(Class<?> clazz) {}

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
