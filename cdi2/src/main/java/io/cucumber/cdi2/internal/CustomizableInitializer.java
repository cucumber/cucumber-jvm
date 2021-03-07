package io.cucumber.cdi2.internal;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.Extension;

import java.lang.annotation.Annotation;
import java.util.Map;

public class CustomizableInitializer extends SeContainerInitializer {
    private final SeContainerInitializer delegate;

    public CustomizableInitializer(final SeContainerInitializer initializer) {
        this.delegate = initializer;
    }

    @Override
    public SeContainerInitializer addBeanClasses(final Class<?>... classes) {
        return delegate.addBeanClasses(classes);
    }

    @Override
    public SeContainerInitializer addPackages(final Class<?>... classes) {
        return delegate.addPackages(classes);
    }

    @Override
    public SeContainerInitializer addPackages(final boolean recursive, final Class<?>... classes) {
        return delegate.addPackages(recursive, classes);
    }

    @Override
    public SeContainerInitializer addPackages(final Package... packages) {
        return delegate.addPackages(packages);
    }

    @Override
    public SeContainerInitializer addPackages(final boolean recursive, final Package... packages) {
        return delegate.addPackages(recursive, packages);
    }

    @Override
    public SeContainerInitializer addExtensions(final Extension... extensions) {
        return delegate.addExtensions(extensions);
    }

    @Override
    public SeContainerInitializer addExtensions(final Class<? extends Extension>... classes) {
        return delegate.addExtensions(classes);
    }

    @Override
    public SeContainerInitializer enableInterceptors(final Class<?>... classes) {
        return delegate.enableInterceptors(classes);
    }

    @Override
    public SeContainerInitializer enableDecorators(final Class<?>... classes) {
        return delegate.enableDecorators(classes);
    }

    @Override
    public SeContainerInitializer selectAlternatives(final Class<?>... classes) {
        return delegate.selectAlternatives(classes);
    }

    @Override
    public SeContainerInitializer selectAlternativeStereotypes(final Class<? extends Annotation>... classes) {
        return delegate.selectAlternativeStereotypes(classes);
    }

    @Override
    public SeContainerInitializer addProperty(final String key, final Object value) {
        return delegate.addProperty(key, value);
    }

    @Override
    public SeContainerInitializer setProperties(final Map<String, Object> map) {
        return delegate.setProperties(map);
    }

    @Override
    public SeContainerInitializer disableDiscovery() {
        return delegate.disableDiscovery();
    }

    @Override
    public SeContainerInitializer setClassLoader(final ClassLoader classLoader) {
        return delegate.setClassLoader(classLoader);
    }

    @Override
    public SeContainer initialize() {
        throw new UnsupportedOperationException("Can't call initialize from a customizer");
    }
}
