package io.cucumber.jakarta.cdi;

import io.cucumber.core.backend.ObjectFactory;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Unmanaged;
import org.apiguardian.api.API;

import java.util.HashMap;
import java.util.Map;

@API(status = API.Status.STABLE)
public final class CdiJakartaFactory implements ObjectFactory {

    private final Map<Class<?>, Unmanaged.UnmanagedInstance<?>> standaloneInstances = new HashMap<>();
    private SeContainerInitializer initializer;
    private SeContainer container;

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

    private SeContainerInitializer getInitializer() {
        if (initializer == null) {
            initializer = SeContainerInitializer.newInstance();
        }
        return initializer;
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
            BeanManager beanManager = container.getBeanManager();
            Unmanaged<T> unmanaged = new Unmanaged<>(beanManager, type);
            Unmanaged.UnmanagedInstance<T> value = unmanaged.newInstance();
            value.produce();
            value.inject();
            value.postConstruct();
            standaloneInstances.put(type, value);
            return value.get();
        }
        return selected.get();
    }

}
