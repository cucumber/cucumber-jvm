package io.cucumber.core.runtime;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.reflection.Reflections;
import io.cucumber.core.runner.Options;

import java.util.Locale;

public final class ScanningTypeRegistryConfigurerSupplier implements TypeRegistryConfigurerSupplier {

    private final ClassFinder classFinder;
    private final Options options;

    public ScanningTypeRegistryConfigurerSupplier(ClassFinder classFinder, Options options) {
        this.classFinder = classFinder;
        this.options = options;
    }

    @Override
    public TypeRegistryConfigurer get() {
        Reflections reflections = new Reflections(classFinder);
        return reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, options.getGlue(), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
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
