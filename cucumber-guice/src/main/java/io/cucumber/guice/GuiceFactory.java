package io.cucumber.guice;

import com.google.inject.Injector;
import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.resource.ClasspathSupport;
import org.apiguardian.api.API;

import java.util.Collection;
import java.util.HashSet;

import static io.cucumber.guice.InjectorSourceFactory.loadInjectorSourceFromProperties;
import static java.lang.String.format;

/**
 * Guice implementation of the
 * <code>io.cucumber.core.backend.ObjectFactory</code>.
 */
@API(status = API.Status.STABLE)
public final class GuiceFactory implements ObjectFactory {

    private Injector injector;

    private final Collection<Class<?>> stepClasses = new HashSet<>();
    private final Class<?> injectorSourceFromProperty;
    private Class<?> withInjectorSource = null;

    public GuiceFactory() {
        injectorSourceFromProperty = loadInjectorSourceFromProperties(CucumberProperties.create());
    }

    @Override
    public boolean addClass(final Class<?> stepClass) {
        if (stepClasses.contains(stepClass)) {
            return true;
        }
        if (injectorSourceFromProperty == null) {
            if (hasInjectorSource(stepClass)) {
                checkOnlyOneClassHasInjectorSource(stepClass);
                withInjectorSource = stepClass;
            }
        }
        stepClasses.add(stepClass);
        return true;
    }

    private boolean hasInjectorSource(Class<?> stepClass) {
        return InjectorSource.class.isAssignableFrom(stepClass);
    }

    private void checkOnlyOneClassHasInjectorSource(Class<?> stepClass) {
        if (withInjectorSource != null) {
            throw new CucumberBackendException(format("" +
                    "Glue class %1$s and %2$s are both implementing io.cucumber.guice.InjectorSource.\n" +
                    "Please ensure only one class configures the Guice context\n" +
                    "\n" +
                    "By default Cucumber scans the entire classpath for context configuration.\n" +
                    "You can restrict this by configuring the glue path.\n" +
                    ClasspathSupport.configurationExamples(),
                stepClass,
                withInjectorSource));
        }
    }

    void setInjector(Injector injector) {
        this.injector = injector;
    }

    public void start() {
        if (injector == null) {
            injector = new InjectorSourceFactory(withInjectorSource).create()
                    .getInjector();
        }
        injector.getInstance(ScenarioScope.class).enterScope();
    }

    public void stop() {
        injector.getInstance(ScenarioScope.class).exitScope();
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

}
