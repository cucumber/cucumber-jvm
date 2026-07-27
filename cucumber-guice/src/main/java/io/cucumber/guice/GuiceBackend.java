package io.cucumber.guice;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelectorResolver;
import io.cucumber.core.resource.ClasspathScanner;

import java.util.function.Supplier;

final class GuiceBackend implements Backend {

    private final Container container;
    private final GlueDiscoverySelectorResolver resolver;

    GuiceBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.resolver = new GlueDiscoverySelectorResolver(new ClasspathScanner(classLoaderSupplier));
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        resolver.resolve(request)
                .filter(InjectorSource.class::isAssignableFrom)
                .forEach(container::addClass);
    }
}
