package io.cucumber.cdi2;

import io.cucumber.core.backend.ObjectFactory;
import org.apiguardian.api.API;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Unmanaged;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@API(status = API.Status.STABLE)
public final class Cdi2Factory implements ObjectFactory, Extension {

    private final Set<Class<?>> stepClasses = new HashSet<>();

    private final Map<Class<?>, Unmanaged.UnmanagedInstance<?>> standaloneInstances = new HashMap<>();
    private SeContainer container;

    @Override
    public void start() {
        if (container == null) {
            SeContainerInitializer initializer = SeContainerInitializer.newInstance();
            initializer.addExtensions(this);
            container = initializer.initialize();
        }
    }

    @Override
    public void stop() {
        if (container != null) {
            container.close();
            container = null;
        }
        for (Unmanaged.UnmanagedInstance<?> unmanaged : standaloneInstances.values()) {
            unmanaged.preDestroy();
            unmanaged.dispose();
        }
        standaloneInstances.clear();
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        stepClasses.add(clazz);
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        Unmanaged.UnmanagedInstance<?> instance = standaloneInstances.get(type);
        if (instance != null) {
            return type.cast(instance.get());
        }
        Instance<T> selected = container.select(type);
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

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager bm) {
        Set<Class<?>> unmanaged = new HashSet<>();
        for (Class<?> stepClass : stepClasses) {
            discoverUnmanagedTypes(afterBeanDiscovery, bm, unmanaged, stepClass);
        }
    }

    private void discoverUnmanagedTypes(
            AfterBeanDiscovery afterBeanDiscovery, BeanManager bm, Set<Class<?>> unmanaged, Class<?> candidate
    ) {
        if (unmanaged.contains(candidate) || !bm.getBeans(candidate).isEmpty()) {
            return;
        }
        unmanaged.add(candidate);

        addBean(afterBeanDiscovery, bm, candidate);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addBean(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager, Class<?> clazz) {
        AnnotatedType clazzAnnotatedType = beanManager.createAnnotatedType(clazz);
        // @formatter:off
        InjectionTarget injectionTarget = beanManager
                .getInjectionTargetFactory(clazzAnnotatedType)
                .createInjectionTarget(null);
        // @formatter:on
        // @formatter:off
        afterBeanDiscovery.addBean()
            .read(clazzAnnotatedType)
            .createWith(callback -> {
                CreationalContext c = (CreationalContext) callback;
                Object instance = injectionTarget.produce(c);
                injectionTarget.inject(instance, c);
                injectionTarget.postConstruct(instance);
                return instance;
            });
        // @formatter:on
    }

}
