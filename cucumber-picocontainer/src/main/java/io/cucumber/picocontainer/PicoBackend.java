package io.cucumber.picocontainer;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static java.util.Arrays.stream;

final class PicoBackend implements Backend {

    private final Container container;
    private final ClasspathScanner classFinder;

    PicoBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        gluePaths.stream()
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream)
                .filter(clazz -> clazz.isAnnotationPresent(PicoConfiguration.class))
                .distinct()
                .forEach(picoConfig -> {
                    PicoConfiguration configuration = picoConfig.getAnnotation(PicoConfiguration.class);
                    stream(configuration.providers()).forEach(container::addClass);
                    stream(configuration.providerAdapters()).forEach(container::addClass);
                });
    }

    @Override
    public void buildWorld() {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public Snippet getSnippet() {
        return null;
    }

}
