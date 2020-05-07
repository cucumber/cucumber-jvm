package io.cucumber.core.runtime;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.core.runner.Options;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static java.util.stream.Collectors.toSet;

public final class ScanningTypeRegistryConfigurerSupplier implements TypeRegistryConfigurerSupplier {

    private final Reflections reflections;
    private final Options options;

    public ScanningTypeRegistryConfigurerSupplier(Supplier<ClassLoader> classLoader, Options options) {
        this.options = options;
        this.reflections = new Reflections(new ClasspathScanner(classLoader));
    }

    @Override
    public TypeRegistryConfigurer get() {
        return reflections.instantiateExactlyOneSubclass(
            TypeRegistryConfigurer.class,
            options.getGlue(),
            new DefaultTypeRegistryConfiguration());
    }

    private static final class DefaultTypeRegistryConfiguration implements TypeRegistryConfigurer {

        @Override
        public Locale locale() {
            return Locale.ENGLISH;
        }

        @Override
        public void configureTypeRegistry(io.cucumber.core.api.TypeRegistry typeRegistry) {
            // noop
        }

    }

    static final class Reflections {

        private final ClasspathScanner classFinder;

        Reflections(ClasspathScanner classFinder) {
            this.classFinder = classFinder;
        }

        <T> T instantiateExactlyOneSubclass(Class<T> parentType, List<URI> packageNames, T fallback) {
            Collection<? extends T> instances = instantiateSubclasses(parentType, packageNames);
            if (instances.size() == 1) {
                return instances.iterator().next();
            } else if (instances.isEmpty()) {
                if (fallback != null) {
                    return fallback;
                }
                throw new NoInstancesException(parentType);
            } else {
                throw new TooManyInstancesException(instances);
            }
        }

        private <T> Collection<? extends T> instantiateSubclasses(Class<T> parentType, List<URI> packageNames) {
            return packageNames
                    .stream()
                    .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                    .map(ClasspathSupport::packageName)
                    .map(basePackageName -> classFinder.scanForSubClassesInPackage(basePackageName, parentType))
                    .flatMap(Collection::stream)
                    .filter(Reflections::isInstantiable)
                    .map(Reflections::newInstance)
                    .collect(toSet());
        }

        static boolean isInstantiable(Class<?> clazz) {
            boolean isNonStaticInnerClass = !Modifier.isStatic(clazz.getModifiers())
                    && clazz.getEnclosingClass() != null;
            return Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())
                    && !isNonStaticInnerClass;
        }

        private static <T> T newInstance(Class<? extends T> clazz) {
            Constructor<? extends T> constructor;
            try {
                constructor = clazz.getConstructor();
                try {
                    return constructor.newInstance();
                } catch (Exception e) {
                    throw new CucumberException("Failed to instantiate " + constructor.toGenericString(), e);
                }
            } catch (NoSuchMethodException e) {
                throw new CucumberException(e);
            }
        }

    }

    static final class NoInstancesException extends CucumberException {

        NoInstancesException(Class<?> parentType) {
            super(createMessage(parentType));
        }

        private static String createMessage(Class<?> parentType) {
            return String.format("Couldn't find a single implementation of %s", parentType);
        }

    }

    static final class TooManyInstancesException extends CucumberException {

        TooManyInstancesException(Collection<?> instances) {
            super(createMessage(instances));
        }

        private static String createMessage(Collection<?> instances) {
            Objects.requireNonNull(instances);
            return String.format("Expected only one instance, but found too many: %s", instances);
        }

    }

}
