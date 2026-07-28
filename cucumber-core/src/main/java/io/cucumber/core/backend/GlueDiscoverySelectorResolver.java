package io.cucumber.core.backend;

import io.cucumber.core.backend.GlueDiscoveryFilter.ClassNameFilter;
import io.cucumber.core.resource.ClassFilter;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;

public final class GlueDiscoverySelectorResolver {

    private final ClasspathScanner classFinder;
    private final Predicate<Class<?>> classPredicate;

    public GlueDiscoverySelectorResolver(ClasspathScanner classFinder, Predicate<Class<?>> classPredicate) {
        this.classFinder = Objects.requireNonNull(classFinder);
        this.classPredicate = classPredicate;
    }

    public Stream<Class<?>> resolve(GlueDiscoveryRequest request) {
        var classNameFilters = request.getFiltersByType(ClassNameFilter.class);
        var classNamePredicate = classNameFilters.stream()
                .map(filter -> (Predicate<String>) filter::apply)
                .reduce(className -> true, Predicate::and);

        var classesInPackage = request.getSelectorsByType(GlueDiscoverySelector.UriGlueDiscoverySelector.class) //
                .stream() //
                .map(GlueDiscoverySelector.UriGlueDiscoverySelector::uri) //
                .toList().stream() //
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(packageName -> classFinder.scanForClassesInPackage(packageName,
                    ClassFilter.of(classNamePredicate, classPredicate)))
                .flatMap(Collection::stream);

        var explicitClasses = request.getSelectorsByType(GlueDiscoverySelector.ClassGlueDiscoverySelector.class)
                .stream()
                .map(GlueDiscoverySelector.ClassGlueDiscoverySelector::name)
                .map(classFinder::loadClass);

        return Stream.concat(classesInPackage, explicitClasses).distinct();
    }

}
