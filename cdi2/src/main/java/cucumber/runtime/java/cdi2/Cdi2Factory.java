package cucumber.runtime.java.cdi2;

import io.cucumber.core.backend.ObjectFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.Unmanaged;
import java.util.HashMap;
import java.util.Map;

public class Cdi2Factory implements ObjectFactory {

    protected SeContainerInitializer initializer;
    protected SeContainer container;
    private final Map<Class<?>, Unmanaged.UnmanagedInstance<?>> standaloneInstances = new HashMap<>();

    @Override
    public void start() {
        container = getInitializer().initialize();
    }

    @Override
    public void stop() {
        if (container != null) {
            container.close();
            container = null;
            initializer = null;
        }
        for (final Unmanaged.UnmanagedInstance<?> unmanaged : standaloneInstances.values()) {
            unmanaged.preDestroy();
            unmanaged.dispose();
        }
        standaloneInstances.clear();
    }

    @Override
    public boolean addClass(final Class<?> clazz) {
        getInitializer().addBeanClasses(clazz);
        return true;
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        final Object instance = standaloneInstances.get(type);
        if (instance != null) {
            return type.cast(instance);
        }
        final Instance<T> selected = container.select(type);
        if (selected.isUnsatisfied()) {
            final Unmanaged.UnmanagedInstance<T> value = new Unmanaged<>(container.getBeanManager(), type).newInstance();
            value.produce();
            value.inject();
            value.postConstruct();
            standaloneInstances.put(type, value);
            return value.get();
        }
        return selected.get();
    }

    private SeContainerInitializer getInitializer() {
        if (initializer == null) {
            initializer = SeContainerInitializer.newInstance();
        }
        return initializer;
    }
}
