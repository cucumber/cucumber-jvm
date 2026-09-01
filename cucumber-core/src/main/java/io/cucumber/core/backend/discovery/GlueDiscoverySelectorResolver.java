package io.cucumber.core.backend.discovery;

import io.cucumber.core.resource.ClassFilter;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;
import org.apiguardian.api.API;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;

/**
 * Resolves a {@link GlueDiscoveryRequest} into a stream of classes.
 */
@API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
public final class GlueDiscoverySelectorResolver {

    private final ClasspathScanner classFinder;
    private final Predicate<Class<?>> classPredicate;

    public GlueDiscoverySelectorResolver(ClasspathScanner classFinder, Predicate<Class<?>> classPredicate) {
        this.classFinder = Objects.requireNonNull(classFinder);
        this.classPredicate = classPredicate;
    }

    public Stream<Class<?>> resolve(GlueDiscoveryRequest request) {
        var classNameFilters = request.getFiltersByType(GlueClassNameFilter.class);
        var classNamePredicate = classNameFilters.stream()
                .map(filter -> (Predicate<String>) filter::apply)
                .reduce(className -> true, Predicate::and);

        var classesInPackage = request.getSelectorsByType(UriGlueDiscoverySelector.class) //
                .stream() //
                .map(UriGlueDiscoverySelector::uri) //
                .toList().stream() //
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(packageName -> classFinder.scanForClassesInPackage(packageName,
                    ClassFilter.of(classNamePredicate, classPredicate)))
                .flatMap(Collection::stream);

        var explicitClasses = request.getSelectorsByType(ClassGlueDiscoverySelector.class)
                .stream()
                .map(ClassGlueDiscoverySelector::name)
                .map(classFinder::loadClass);

        return Stream.concat(classesInPackage, explicitClasses).distinct();
    }

}
