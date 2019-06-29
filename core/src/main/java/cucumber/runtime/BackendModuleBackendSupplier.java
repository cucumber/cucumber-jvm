package cucumber.runtime;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.runtime.io.ResourceLoader;
import io.cucumber.core.options.RunnerOptions;
import io.cucumber.stepexpression.TypeRegistry;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;


/**
 * Supplies instances of {@link Backend} found by scanning {@code cucumber.runtime} for implementations.
 */
public final class BackendModuleBackendSupplier implements BackendSupplier {

    private static final List<URI> backendPackages = singletonList(URI.create("classpath:cucumber/runtime"));
    private final ResourceLoader resourceLoader;
    private final ClassFinder classFinder;
    private final RunnerOptions runnerOptions;
    private final List<URI> packages;

    public BackendModuleBackendSupplier(ResourceLoader resourceLoader, ClassFinder classFinder, RunnerOptions runnerOptions) {
        this(resourceLoader, classFinder, runnerOptions, backendPackages);
    }

    BackendModuleBackendSupplier(ResourceLoader resourceLoader, ClassFinder classFinder, RunnerOptions runnerOptions, List<URI> packages) {
        this.resourceLoader = resourceLoader;
        this.classFinder = classFinder;
        this.runnerOptions = runnerOptions;
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

        TypeRegistry typeRegistry;

        io.cucumber.core.api.TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(io.cucumber.core.api.TypeRegistryConfigurer.class, runnerOptions.getGlue(), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
        if (typeRegistryConfigurer.getClass() != DefaultTypeRegistryConfiguration.class) {
            typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
            typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
        } else {
            TypeRegistryConfigurer typeRegistryConfigurer2 = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, runnerOptions.getGlue(), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
            typeRegistry = new TypeRegistry(typeRegistryConfigurer2.locale());
            typeRegistryConfigurer2.configureTypeRegistry(typeRegistry);
        }

        return reflections.instantiateSubclasses(Backend.class, packages, new Class[]{ResourceLoader.class, TypeRegistry.class}, new Object[]{resourceLoader, typeRegistry});
    }

}
