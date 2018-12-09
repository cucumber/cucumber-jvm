package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;

import static io.cucucumber.jupiter.engine.ClasspathScanningSupport.buildPackageFilter;
import static io.cucucumber.jupiter.engine.FeatureResolver.createFeatureResolver;

class DiscoverySelectorResolver {

    void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
        Filter<String> packageFilter = buildPackageFilter(request);
        resolve(request, engineDescriptor, packageFilter);
        filter(engineDescriptor, packageFilter);
        pruneTree(engineDescriptor);
    }

    private void resolve(EngineDiscoveryRequest request, TestDescriptor engineDescriptor, Filter<String> packageFilter) {
        FeatureResolver featureResolver = createFeatureResolver(engineDescriptor, packageFilter);

        request.getSelectorsByType(ClasspathRootSelector.class).forEach(featureResolver::resolveClasspathRoot);
        request.getSelectorsByType(ClasspathResourceSelector.class).forEach(featureResolver::resolveClasspathResource);
        request.getSelectorsByType(PackageSelector.class).forEach(featureResolver::resolvePackageResource);
        request.getSelectorsByType(FileSelector.class).forEach(featureResolver::resolveFile);
        request.getSelectorsByType(DirectorySelector.class).forEach(featureResolver::resolveDirectory);
        request.getSelectorsByType(UniqueIdSelector.class).forEach(featureResolver::resolveUniqueId);
        request.getSelectorsByType(UriSelector.class).forEach(featureResolver::resolveUri);
    }

    private void filter(TestDescriptor engineDescriptor, Filter<String> packageFilter) {
        new DiscoveryFilterApplier().applyPackagePredicate(packageFilter.toPredicate(), engineDescriptor);
    }

    private void pruneTree(TestDescriptor rootDescriptor) {
        rootDescriptor.accept(TestDescriptor::prune);
    }

}
