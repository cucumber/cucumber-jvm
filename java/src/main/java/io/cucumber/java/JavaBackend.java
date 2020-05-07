package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.net.URI;
import java.util.Collection;
import java.util.List;
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
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        GlueAdaptor glueAdaptor = new GlueAdaptor(lookup, glue);

        gluePaths.stream()
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream)
                .forEach(aGlueClass -> scan(aGlueClass, (method, annotation) -> {
                    container.addClass(method.getDeclaringClass());
                    glueAdaptor.addDefinition(method, annotation);
                }));
    }

    @Override
    public void buildWorld() {

    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public Snippet getSnippet() {
        return new JavaSnippet();
    }

}
