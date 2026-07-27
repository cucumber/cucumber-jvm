package io.cucumber.spring;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelectorResolver;
import io.cucumber.core.resource.ClasspathScanner;

import java.lang.reflect.Modifier;
import java.util.function.Supplier;

final class SpringBackend implements Backend {

    private final Container container;
    private final GlueDiscoverySelectorResolver resolver;

    SpringBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.resolver = new GlueDiscoverySelectorResolver(new ClasspathScanner(classLoaderSupplier));
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        resolver.resolve(request)
                .filter(SpringFactory::hasCucumberContextConfiguration)
                .filter(this::checkIfOfClassTypeAndNotAbstract)
                .forEach(container::addClass);
    }

    private boolean checkIfOfClassTypeAndNotAbstract(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }
}
