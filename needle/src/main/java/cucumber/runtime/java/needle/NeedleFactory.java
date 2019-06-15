package cucumber.runtime.java.needle;

import cucumber.runtime.java.needle.config.CollectInjectionProvidersFromStepsInstance;
import cucumber.runtime.java.needle.config.CreateInstanceByDefaultConstructor;
import cucumber.runtime.java.needle.config.CucumberNeedleConfiguration;
import de.akquinet.jbosscc.needle.NeedleTestcase;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import io.cucumber.core.backend.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static cucumber.runtime.java.needle.config.CucumberNeedleConfiguration.RESOURCE_CUCUMBER_NEEDLE;
import static java.lang.String.format;

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
        try {
            // First create all instances
            for (final Class<?> stepDefinitionType : cachedStepsInstances.keySet()) {
                cachedStepsInstances.put(stepDefinitionType, createStepsInstance(stepDefinitionType));
            }
            // Then collect injection providers from all instances
            for (Object stepsInstance : cachedStepsInstances.values()) {
                addInjectionProvider(collectInjectionProvidersFromStepsInstance.apply(stepsInstance));
            }
            // Now init all instances, having the injection providers from all other instances available
            for (Object stepsInstance : cachedStepsInstances.values()) {
                initTestcase(stepsInstance);
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e);
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
    public boolean addClass(final Class<?> type) {
        logger.trace("addClass(): " + type.getCanonicalName());

        // build up cache keys ...
        if (!cachedStepsInstances.containsKey(type)) {
            cachedStepsInstances.put(type, null);
        }
        return true;
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

    private <T> T createStepsInstance(final Class<T> type) throws Exception {
        logger.trace("createInstance(): " + type.getCanonicalName());
        return createInstanceByDefaultConstructor.apply(type);
    }

    static InjectionProvider<?>[] setUpInjectionProviders(final String resourceName) {
        return new CucumberNeedleConfiguration(resourceName).getInjectionProviders();
    }
}
