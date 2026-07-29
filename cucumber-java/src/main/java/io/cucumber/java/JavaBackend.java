package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelectorResolver;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;

import java.util.function.Supplier;

import static io.cucumber.java.MethodScanner.scan;

final class JavaBackend implements Backend {

    private final Lookup lookup;
    private final Container container;
    private final GlueDiscoverySelectorResolver resolver;

    JavaBackend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.lookup = lookup;
        this.container = container;
        this.resolver = new GlueDiscoverySelectorResolver( //
            new ClasspathScanner(classLoaderSupplier), //
            anyClass -> true //
        );
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        GlueAdaptor glueAdaptor = new GlueAdaptor(lookup, glue);
        try (var advisor = new GlueLoadingAdvisor(request)) {
            resolver.resolve(request)
                    .forEach(aGlueClass -> {
                        advisor.addGlueClass(aGlueClass);
                        scan(aGlueClass, (method, annotation) -> {
                            advisor.addContainerClass(method.getDeclaringClass());
                            container.addClass(method.getDeclaringClass());
                            glueAdaptor.addDefinition(method, annotation);
                        });
                    });

        }
    }

    @Override
    public Snippet getSnippet() {
        return new JavaSnippet();
    }

}
