package io.cucucumber.jupiter.engine;

import io.cucucumber.jupiter.engine.resource.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.discovery.PackageNameFilter;

import static org.junit.platform.engine.Filter.composeFilters;

final class ClasspathScanningSupport {

    private ClasspathScanningSupport() {
    }

    static ClassFilter buildPackageFilter(EngineDiscoveryRequest request) {
        Filter<String> packageFilter = composeFilters(request.getFiltersByType(PackageNameFilter.class));
        return ClassFilter.of(packageFilter.toPredicate(), aClass -> true);
    }

}
