package io.cucumber.picocontainer;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelectorResolver;
import io.cucumber.core.resource.ClasspathScanner;

import java.util.function.Supplier;

final class PicoBackend implements Backend {

    private final Container container;
    private final GlueDiscoverySelectorResolver resolver;

    PicoBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.resolver = new GlueDiscoverySelectorResolver(new ClasspathScanner(classLoaderSupplier));
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        resolver.resolve(request)
                .filter(PicoBackend::hasCucumberPicoProvider)
                .forEach(container::addClass);
    }

    private static boolean hasCucumberPicoProvider(Class<?> clazz) {
        return clazz.isAnnotationPresent(CucumberPicoProvider.class);
    }

}
