package io.cucucumber.jupiter.engine;

import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.PackageNameFilter;

import java.util.function.Predicate;

import static org.junit.platform.engine.Filter.composeFilters;

final class ClasspathScanningSupport {

    private ClasspathScanningSupport() {
    }

    private static Predicate<String> buildClassNamePredicate(EngineDiscoveryRequest request) {
        return composeFilters(request.getFiltersByType(PackageNameFilter.class)).toPredicate();
    }

    static ClassFilter buildPackageFilter(EngineDiscoveryRequest request, Predicate<Class<?>> classPredicate) {
        return ClassFilter.of(buildClassNamePredicate(request), classPredicate);
    }

}
