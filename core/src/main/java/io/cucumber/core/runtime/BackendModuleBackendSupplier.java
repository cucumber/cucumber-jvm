package io.cucumber.core.runtime;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.Reflections;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.singletonList;


/**
 * Supplies instances of {@link Backend} found by scanning {@code cucumber.runtime} for implementations.
 */
public final class BackendModuleBackendSupplier implements BackendSupplier {

    private final ResourceLoader resourceLoader;
    private final ClassFinder classFinder;
    private final RuntimeOptions runtimeOptions;
    private final List<String> packages;

    public BackendModuleBackendSupplier(ResourceLoader resourceLoader, ClassFinder classFinder, RuntimeOptions runtimeOptions) {
        this(resourceLoader, classFinder, runtimeOptions, singletonList("io.cucumber"));
    }

    BackendModuleBackendSupplier(ResourceLoader resourceLoader, ClassFinder classFinder, RuntimeOptions runtimeOptions, List<String> packages) {
        this.resourceLoader = resourceLoader;
        this.classFinder = classFinder;
        this.runtimeOptions = runtimeOptions;
        this.packages = packages;
    }

    @Override
    public Collection<? extends Backend> get() {
        Collection<? extends Backend> backends = loadBackends();
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        return backends;
    }

    private Collection<? extends Backend> loadBackends() {
        Reflections reflections = new Reflections(classFinder);
        TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, MultiLoader.packageName(runtimeOptions.getGlue()), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
        TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
        typeRegistryConfigurer.configureTypeRegistry(typeRegistry);

        return reflections.instantiateSubclasses(Backend.class, packages, new Class[]{ResourceLoader.class, TypeRegistry.class}, new Object[]{resourceLoader, typeRegistry});
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
