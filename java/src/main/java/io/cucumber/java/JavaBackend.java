package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.backend.Snippet;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;

final class JavaBackend implements Backend {

    private final Lookup lookup;
    private final Container container;
    private final ClassFinder classFinder;

    JavaBackend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.lookup = lookup;
        this.container = container;
        ClassLoader classLoader = classLoaderSupplier.get();
        MultiLoader resourceLoader = new MultiLoader(classLoader);
        this.classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        GlueAdaptor glueAdaptor = new GlueAdaptor(lookup, glue);
        for (URI gluePath : gluePaths) {
            for (Class<?> glueCodeClass : classFinder.getDescendants(Object.class, gluePath)) {
                MethodScanner.scan(glueCodeClass, (method, annotation) -> {
                    container.addClass(method.getDeclaringClass());
                    glueAdaptor.addDefinition(method, annotation);
                });
            }
        }
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
