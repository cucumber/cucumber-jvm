package io.cucumber.cdi2;

import io.cucumber.cdi2.internal.CustomizableInitializer;
import io.cucumber.cdi2.spi.SeContainerInitializerCustomizer;
import io.cucumber.core.backend.ObjectFactory;
import org.apiguardian.api.API;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.Unmanaged;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@API(status = API.Status.STABLE)
public final class Cdi2Factory implements ObjectFactory {

    private final Map<Class<?>, Unmanaged.UnmanagedInstance<?>> standaloneInstances = new HashMap<>();
    private SeContainerInitializer initializer;
    private SeContainer container;

    @Override
    public void start() {
        final SeContainerInitializer initializer = getInitializer();
        final SeContainerInitializer limitedInitializer = new CustomizableInitializer(initializer);
        ServiceLoader.load(SeContainerInitializerCustomizer.class)
                .forEach(customizer -> customizer.customize(limitedInitializer));
        systemPropertiesConfiguration(limitedInitializer);
        container = initializer.initialize();
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

    private void systemPropertiesConfiguration(final SeContainerInitializer initializer) {
        System.getProperties()
                .stringPropertyNames().stream()
                .filter(k -> k.startsWith("cucumber.cdi2."))
                .forEach(key -> {
                    switch (key) {
                        case "cucumber.cdi2.packages":
                            ofNullable(System.getProperty(key))
                                    .map(this::toPackages)
                                    .ifPresent(initializer::addPackages);
                            break;
                        case "cucumber.cdi2.classes":
                            ofNullable(System.getProperty(key))
                                    .map(this::toClasses)
                                    .ifPresent(initializer::addBeanClasses);
                            break;
                        case "cucumber.cdi2.extensions":
                            ofNullable(System.getProperty(key))
                                    .map(this::toClasses)
                                    .map(exts -> (Class<? extends Extension>[]) exts)
                                    .ifPresent(initializer::addExtensions);
                            break;
                        case "cucumber.cdi2.recursivePackages":
                            ofNullable(System.getProperty(key))
                                    .map(this::toPackages)
                                    .ifPresent(packages -> initializer.addPackages(true, packages));
                            break;
                        default:
                            initializer.addProperty(key.substring("cucumber.cdi2.".length()), System.getProperty(key));
                    }
                });
    }

    private Class<?>[] toClasses(final String csv) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return csv2Values(csv)
                .map(n -> {
                    try {
                        return loader.loadClass(n);
                    } catch (final ClassNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .toArray(Class<?>[]::new);
    }

    private Package[] toPackages(final String csv) {
        return csv2Values(csv)
                .map(this::findPackage)
                .toArray(Package[]::new);
    }

    private Stream<String> csv2Values(final String csv) {
        return Stream.of(csv.split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .distinct();
    }

    // trivial heuristic for now
    private Package findPackage(final String name) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name + ".package-info").getPackage();
        } catch (final ClassNotFoundException e) {
            // here we can miss it if no class was loaded, this is why we prefer
            // package-info
            try {
                final Method getDefinedPackage = ClassLoader.class.getMethod("getDefinedPackage", String.class);
                if (!getDefinedPackage.isAccessible()) {
                    getDefinedPackage.setAccessible(true);
                }
                final Object pck = getDefinedPackage.invoke(Thread.currentThread().getContextClassLoader(), name);
                if (Package.class.isInstance(pck)) {
                    return Package.class.cast(pck);
                }
            } catch (final NoSuchMethodException noSuchMethodException) {
                final Package pck = Package.getPackage(name);
                if (pck != null) {
                    return pck;
                }
            } catch (final InvocationTargetException | IllegalAccessException illegalAccessException) {
                // no-op, let's fail
            }
            throw new IllegalArgumentException("Invalid package '" + name + "', missing package-info.class");
        }
    }

    private SeContainerInitializer getInitializer() {
        if (initializer == null) {
            initializer = SeContainerInitializer.newInstance();
        }
        return initializer;
    }

    @Override
    public boolean addClass(final Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        final Unmanaged.UnmanagedInstance<?> instance = standaloneInstances.get(type);
        if (instance != null) {
            return type.cast(instance.get());
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
