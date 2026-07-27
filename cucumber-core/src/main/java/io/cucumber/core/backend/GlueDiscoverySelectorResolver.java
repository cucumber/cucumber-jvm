package io.cucumber.core.backend;

import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;

public final class GlueDiscoverySelectorResolver {

    private final ClasspathScanner classFinder;

    public GlueDiscoverySelectorResolver(ClasspathScanner classFinder) {
        this.classFinder = Objects.requireNonNull(classFinder);
    }

    // TODO: Support filtering for class conditions.
    // TODO: SUpport filtering before class loading (e.g. files named *StepDefinion).
    public Stream<Class<?>> resolve(GlueDiscoveryRequest request) {
        var classesInPackage = request.getSelectorsByType(GlueDiscoverySelector.UriGlueDiscoverySelector.class) //
                .stream() //
                .map(GlueDiscoverySelector.UriGlueDiscoverySelector::uri) //
                .toList().stream() //
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream);

        var explicitClasses = request.getSelectorsByType(GlueDiscoverySelector.ClassGlueDiscoverySelector.class)
                .stream()
                .map(GlueDiscoverySelector.ClassGlueDiscoverySelector::name)
                .map(classFinder::loadClass);

        return Stream.concat(classesInPackage, explicitClasses).distinct();
    }

}
