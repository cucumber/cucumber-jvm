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
import java.util.stream.Stream;

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
    public void loadGlue(Glue glue, GlueDiscoveryRequest glueDiscoveryRequest) {
        Stream<Class<?>> classesFromUris = glueDiscoveryRequest.getSelectorsByType(GlueDiscoverySelector.UriGlueDiscoverySelector.class)
                .stream()
                .map(GlueDiscoverySelector.UriGlueDiscoverySelector::getUri)
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream);

        Stream<Class<?>> classNames = glueDiscoveryRequest.getSelectorsByType(GlueDiscoverySelector.ClassGlueDiscoverySelector.class)
                .stream()
                .map(GlueDiscoverySelector.ClassGlueDiscoverySelector::getClassName)
                .map(classFinder::loadClass);

        GlueAdaptor glueAdaptor = new GlueAdaptor(lookup, glue);

        Stream.concat(classesFromUris, classNames)
                .distinct()
                .forEach(aGlueClass -> scan(aGlueClass, (method, annotation) -> {
                    container.addClass(method.getDeclaringClass());
                    glueAdaptor.addDefinition(method, annotation);
                }));
    }


    @Override
    public Snippet getSnippet() {
        return new JavaSnippet();
    }

}
