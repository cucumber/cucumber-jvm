package io.cucumber.core.runtime;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.Reflections;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

/**
 * Supplies instances of {@link Backend} created by using a {@link ServiceLoader}
 * to locate instance of {@link BackendSupplier}.
 */
public final class BackendServiceLoader implements BackendSupplier {

    private final ResourceLoader resourceLoader;
    private final ClassFinder classFinder;
    private final RuntimeOptions runtimeOptions;

    public BackendServiceLoader(ResourceLoader resourceLoader, ClassFinder classFinder, RuntimeOptions runtimeOptions) {
        this.resourceLoader = resourceLoader;
        this.classFinder = classFinder;
        this.runtimeOptions = runtimeOptions;
    }

    @Override
    public Collection<? extends Backend> get() {
        return get(ServiceLoader.load(BackendProviderService.class));
    }

    Collection<? extends Backend> get(Iterable<BackendProviderService> serviceLoader) {
        Collection<? extends Backend> backends = loadBackends(serviceLoader);
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        return backends;
    }

    private Collection<? extends Backend> loadBackends(Iterable<BackendProviderService> serviceLoader) {
        final TypeRegistry typeRegistry = createTypeRegistry();

        List<Backend> backends = new ArrayList<>();
        for (BackendProviderService backendProviderService : serviceLoader) {
            backends.add(backendProviderService.create(resourceLoader, typeRegistry));
        }
        return backends;
    }

    private TypeRegistry createTypeRegistry() {
        Reflections reflections = new Reflections(classFinder);
        TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, MultiLoader.packageName(runtimeOptions.getGlue()), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
        TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
        typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
        return typeRegistry;
    }

    private static final class DefaultTypeRegistryConfiguration implements TypeRegistryConfigurer {

        @Override
        public Locale locale() {
            return Locale.ENGLISH;
        }

        @Override
        public void configureTypeRegistry(io.cucumber.core.api.TypeRegistry typeRegistry) {
            //noop
        }

    }
}
