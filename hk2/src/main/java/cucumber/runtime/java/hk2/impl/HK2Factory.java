package cucumber.runtime.java.hk2.impl;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Env;
import cucumber.runtime.java.hk2.ScenarioScoped;
import cucumber.runtime.java.hk2.ServiceLocatorSource;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.HashSet;
import java.util.Set;

/**
 * HK2 implementation of the <code>cucumber.api.java.ObjectFactory</code>.
 */
public class HK2Factory implements ObjectFactory {

    private final ServiceLocatorSource locatorSource;

    private Set<Class<?>> cucumberClasses = new HashSet<Class<?>>();
    private boolean initialized = false;

    private ServiceLocator locator;
    private ScenarioContext context;

    public HK2Factory() {
        this(new ServiceLocatorSourceFactory(Env.INSTANCE).create());
    }

    /**
     * Package private constructor that is called by the public constructor at runtime and is also called directly by
     * tests.
     *
     * @param locatorSource a service locator configured with a ScenarioScoped binding
     */
    HK2Factory(final ServiceLocatorSource locatorSource) {
        // Delay making a first call to getServiceLocator() until start(), to allow test rules and setup to run
        this.locatorSource = locatorSource;
    }

    public boolean addClass(Class<?> clazz) {
        cucumberClasses.add(clazz);
        return true;
    }

    public void start() {
        initLocator();
    }

    private void initLocator() {
        if (initialized) {
            return;
        }

        locator = locatorSource.getServiceLocator();

        if (locator == null) {
            throw new CucumberHK2Exception("The ServiceLocator cannot be null!");
        }

        // Bind the scenario scoped context and cucumber classes
        ServiceLocatorUtilities.bind(locator, new AbstractBinder() {
            @Override
            protected void configure() {
                addActiveDescriptor(ScenarioContext.class);

                for (Class<?> clazz : cucumberClasses) {
                    bindAsContract(clazz).in(ScenarioScoped.class);
                }
            }
        });

        context = locator.getService(ScenarioContext.class);

        initialized = true;
    }

    public void stop() {
        context.clear();
    }

    public <T> T getInstance(Class<T> clazz) {
        return locator.getService(clazz);
    }
}
