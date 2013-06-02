package cucumber.runtime.java.needle;

import static cucumber.runtime.java.needle.config.CucumberNeedleConfiguration.RESOURCE_CUCUMBER_NEEDLE;
import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.needle.config.CollectInjectionProvidersFromStepsInstance;
import cucumber.runtime.java.needle.config.CreateInstanceByDefaultConstructor;
import cucumber.runtime.java.needle.config.CucumberNeedleConfiguration;
import de.akquinet.jbosscc.needle.NeedleTestcase;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

/**
 * Needle factory for object resolution inside of cucumber tests.
 */
public class NeedleFactory extends NeedleTestcase implements ObjectFactory {

    private final Map<Class<?>, Object> cachedStepsInstances = new LinkedHashMap<Class<?>, Object>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CreateInstanceByDefaultConstructor createInstanceByDefaultConstructor = CreateInstanceByDefaultConstructor.INSTANCE;
    private final CollectInjectionProvidersFromStepsInstance collectInjectionProvidersFromStepsInstance = CollectInjectionProvidersFromStepsInstance.INSTANCE;

    public NeedleFactory() {
        super(setUpInjectionProviders(RESOURCE_CUCUMBER_NEEDLE));
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        logger.trace("getInstance: " + type.getCanonicalName());
        assertTypeHasBeenAdded(type);
        return nullSafeGetInstance(type);
    }

    @Override
    public void start() {
        logger.trace("start()");
        for (final Class<?> stepDefinitionType : cachedStepsInstances.keySet()) {
            cachedStepsInstances.put(stepDefinitionType, createStepsInstance(stepDefinitionType));
        }
    }

    @Override
    public void stop() {
        logger.trace("stop()");
        for (final Class<?> stepDefinitionType : cachedStepsInstances.keySet()) {
            cachedStepsInstances.put(stepDefinitionType, null);
        }
    }

    @Override
    public void addClass(final Class<?> type) {
        logger.trace("addClass(): " + type.getCanonicalName());

        // build up cache keys ...
        if (!cachedStepsInstances.containsKey(type)) {
            cachedStepsInstances.put(type, null);
        }
    }

    private void assertTypeHasBeenAdded(final Class<?> type) {
        if (!cachedStepsInstances.containsKey(type)) {
            throw new IllegalStateException(format("%s was not added during addClass()", type.getSimpleName()));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T nullSafeGetInstance(final Class<T> type) {
        final Object instance = cachedStepsInstances.get(type);
        if (instance == null) {
            throw new IllegalStateException(format("instance of type %s has not been initialized in start()!",
                    type.getSimpleName()));
        }
        return (T) instance;
    }

    private <T> T createStepsInstance(final Class<T> type) {
        logger.trace("createInstance(): " + type.getCanonicalName());
        try {
            final T stepsInstance = createInstanceByDefaultConstructor.apply(type);
            addInjectionProvider(collectInjectionProvidersFromStepsInstance.apply(stepsInstance));
            initTestcase(stepsInstance);
            return stepsInstance;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static InjectionProvider<?>[] setUpInjectionProviders(final String resourceName) {
        return new CucumberNeedleConfiguration(resourceName).getInjectionProviders();
    }
}
