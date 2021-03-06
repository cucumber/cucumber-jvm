package io.cucumber.jakarta.cdi;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CdiJakartaFactory.class);
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
        Set<Type> unmanaged = new HashSet<>();
        for (Class<?> stepClass : stepClasses) {
            discoverUnmanagedTypes(afterBeanDiscovery, bm, unmanaged, stepClass);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void discoverUnmanagedTypes(
            AfterBeanDiscovery afterBeanDiscovery, BeanManager bm, Set<Type> unmanaged,
            Type candidate
    ) {
        if (unmanaged.contains(candidate) || !bm.getBeans(candidate).isEmpty()) {
            return;
        }
        unmanaged.add(candidate);
        if (!(candidate instanceof Class<?>)) {
            log.warn(() -> "Can not add '" + candidate + "' as an unmanaged bean");
            return;
        }
        InjectionTarget injectionTarget = addBean(afterBeanDiscovery, bm, (Class<?>) candidate);
        Set<InjectionPoint> ips = injectionTarget.getInjectionPoints();
        for (InjectionPoint ip : ips) {
            discoverUnmanagedTypes(afterBeanDiscovery, bm, unmanaged, ip.getType());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private InjectionTarget addBean(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager, Class<?> clazz) {
        AnnotatedType clazzAnnotatedType = beanManager.createAnnotatedType(clazz);
        // @formatter:off
        InjectionTarget injectionTarget = beanManager
                .getInjectionTargetFactory(clazzAnnotatedType)
                .createInjectionTarget(null);
        // @formatter:on
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
