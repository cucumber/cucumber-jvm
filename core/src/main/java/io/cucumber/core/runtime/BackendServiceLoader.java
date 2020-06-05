package io.cucumber.core.runtime;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Supplies instances of {@link Backend} created by using a
 * {@link ServiceLoader} to locate instance of {@link BackendSupplier}.
 */
public final class BackendServiceLoader implements BackendSupplier {

    private final Supplier<ClassLoader> classLoaderSupplier;
    private final ObjectFactorySupplier objectFactorySupplier;

    public BackendServiceLoader(
            Supplier<ClassLoader> classLoaderSupplier, ObjectFactorySupplier objectFactorySupplier
    ) {
        this.classLoaderSupplier = classLoaderSupplier;
        this.objectFactorySupplier = objectFactorySupplier;
    }

    @Override
    public Collection<? extends Backend> get() {
        return get(ServiceLoader.load(BackendProviderService.class));
    }

    Collection<? extends Backend> get(Iterable<BackendProviderService> serviceLoader) {
        Collection<? extends Backend> backends = loadBackends(serviceLoader);
        if (backends.isEmpty()) {
            throw new CucumberException(
                "No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        return backends;
    }

    private Collection<? extends Backend> loadBackends(Iterable<BackendProviderService> serviceLoader) {
        List<Backend> backends = new ArrayList<>();
        for (BackendProviderService backendProviderService : serviceLoader) {
            ObjectFactory objectFactory = objectFactorySupplier.get();
            backends.add(backendProviderService.create(objectFactory, objectFactory, classLoaderSupplier));
        }
        return backends;
    }

}
