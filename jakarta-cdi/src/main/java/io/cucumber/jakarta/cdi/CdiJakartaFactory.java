package io.cucumber.jakarta.cdi;

import io.cucumber.core.backend.ObjectFactory;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Unmanaged;
import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@API(status = API.Status.STABLE)
public final class CdiJakartaFactory implements ObjectFactory, Extension {

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
        Set<Class<?>> unmanagedClasses = new HashSet<>();

        for (Class<?> stepClass : stepClasses) {
            discoverUnmanagedClasses(afterBeanDiscovery, bm, unmanagedClasses, stepClass);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void discoverUnmanagedClasses(
            AfterBeanDiscovery afterBeanDiscovery, BeanManager bm, Set<Class<?>> unmanagedClasses,
            Class<?> clazz
    ) {
        if (unmanagedClasses.contains(clazz) || !bm.getBeans(clazz).isEmpty()) {
            return;
        }
        unmanagedClasses.add(clazz);

        InjectionTarget injectionTarget = addBean(afterBeanDiscovery, bm, clazz);

        Set<InjectionPoint> ips = injectionTarget.getInjectionPoints();
        for (InjectionPoint ip : ips) {
            Type type = ip.getType();
            if (type instanceof Class) {
                discoverUnmanagedClasses(afterBeanDiscovery, bm, unmanagedClasses, (Class<?>) type);
            }
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private InjectionTarget addBean(AfterBeanDiscovery afterBeanDiscovery, BeanManager bm, Class<?> clazz) {
        AnnotatedType clazzAnnotatedType = bm.createAnnotatedType(clazz);
        InjectionTarget injectionTarget = bm.getInjectionTargetFactory(clazzAnnotatedType)
                .createInjectionTarget(null);

        // @formatter:off
        afterBeanDiscovery
                .addBean()
                .read(clazzAnnotatedType)
                .createWith(callback -> {
                    CreationalContext c = (CreationalContext) callback;
                    Object instance = injectionTarget.produce(c);
                    injectionTarget.inject(instance, c);
                    injectionTarget.postConstruct(instance);
                    return instance;
                });
        // @formatter:on
        return injectionTarget;
    }

}
