package io.cucumber.guice;

import com.google.inject.Injector;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberProperties;
import org.apiguardian.api.API;

/**
 * Guice implementation of the
 * <code>io.cucumber.core.backend.ObjectFactory</code>.
 */
@API(status = API.Status.STABLE)
public final class GuiceFactory implements ObjectFactory {

    private final Injector injector;

    public GuiceFactory() {
        this(new InjectorSourceFactory(CucumberProperties.create()).create().getInjector());
    }

    /**
     * Package private constructor that is called by the public constructor at
     * runtime and is also called directly by tests.
     *
     * @param injector an injector configured with a binding for
     *                 <code>ScenarioScope</code>.
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
