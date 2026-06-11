package io.cucumber.guice;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelector;
import io.cucumber.core.backend.GlueDiscoverySelector.UriGlueDiscoverySelector;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;

final class GuiceBackend implements Backend {

    private final Container container;
    private final ClasspathScanner classFinder;

    GuiceBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        var packageClasses = request.getSelectorsByType(UriGlueDiscoverySelector.class).stream()
                .map(UriGlueDiscoverySelector::uri)
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream);

        var explicitClasses = request.getSelectorsByType(GlueDiscoverySelector.ClassGlueDiscoverySelector.class) //
                .stream() //
                .map(GlueDiscoverySelector.ClassGlueDiscoverySelector::name)
                .map(classFinder::loadClass);

        Stream.concat(packageClasses, explicitClasses)
                .filter(InjectorSource.class::isAssignableFrom)
                .distinct()
                .forEach(container::addClass);
    }
}
