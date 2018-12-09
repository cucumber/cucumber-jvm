package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.discovery.PackageNameFilter;

import static org.junit.platform.engine.Filter.composeFilters;

final class ClasspathScanningSupport {

    private ClasspathScanningSupport() {
    }

    static Filter<String> buildPackageFilter(EngineDiscoveryRequest request) {
        return composeFilters(request.getFiltersByType(PackageNameFilter.class));
    }

}
