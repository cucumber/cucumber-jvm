package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelector;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.util.Collection;
import java.util.function.Supplier;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static io.cucumber.java.MethodScanner.scan;

final class JavaBackend implements Backend {

    private final Lookup lookup;
    private final Container container;
    private final ClasspathScanner classFinder;

    JavaBackend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.lookup = lookup;
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        GlueAdaptor glueAdaptor = new GlueAdaptor(lookup, glue);
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(request.getOptions());

        var gluePaths = request.getSelectorsByType(GlueDiscoverySelector.UriGlueDiscoverySelector.class) //
                .stream() //
                .map(GlueDiscoverySelector.UriGlueDiscoverySelector::uri) //
                .toList();

        advisor.glueLoadingStarted();

        gluePaths.stream() //
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream)
                .distinct()
                .forEach(aGlueClass -> {
                    advisor.addGlueClass(aGlueClass);
                    scan(aGlueClass, (method, annotation) -> {
                        advisor.addContainerClass(method.getDeclaringClass());
                        container.addClass(method.getDeclaringClass());
                        glueAdaptor.addDefinition(method, annotation);
                    });
                });

        advisor.logGlueLoadingSuggestions(gluePaths);
    }

    @Override
    public Snippet getSnippet() {
        return new JavaSnippet();
    }

}
