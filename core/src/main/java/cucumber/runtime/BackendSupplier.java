package cucumber.runtime;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import io.cucumber.stepexpression.TypeRegistry;

import java.util.Collection;

import static java.util.Collections.singletonList;

public class BackendSupplier implements Supplier<Collection<? extends Backend>> {

    private final ResourceLoader resourceLoader;
    private final ClassFinder classFinder;
    private final RuntimeOptions runtimeOptions;

    public BackendSupplier(ResourceLoader resourceLoader, ClassFinder classFinder, RuntimeOptions runtimeOptions) {
        this.resourceLoader = resourceLoader;
        this.classFinder = classFinder;
        this.runtimeOptions = runtimeOptions;
    }


    @Override
    public Collection<? extends Backend> get() {
        Collection<? extends Backend> backends = loadBackends(resourceLoader, classFinder, runtimeOptions);

        return backends;
    }

    private static Collection<? extends Backend> loadBackends(ResourceLoader resourceLoader, ClassFinder classFinder, RuntimeOptions runtimeOptions) {
        Reflections reflections = new Reflections(classFinder);
        TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, MultiLoader.packageName(runtimeOptions.getGlue()), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
        TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
        typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
        return reflections.instantiateSubclasses(Backend.class, singletonList("cucumber.runtime"), new Class[]{ResourceLoader.class, TypeRegistry.class}, new Object[]{resourceLoader, typeRegistry});
    }

}
