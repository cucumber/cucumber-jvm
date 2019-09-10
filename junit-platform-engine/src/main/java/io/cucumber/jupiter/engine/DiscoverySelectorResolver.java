package io.cucumber.jupiter.engine;

import io.cucumber.jupiter.engine.resource.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;

class DiscoverySelectorResolver {

    void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
        ClassFilter packageFilter = ClasspathScanningSupport.buildPackageFilter(request);
        resolve(request, engineDescriptor, packageFilter);
        filter(engineDescriptor, packageFilter);
        pruneTree(engineDescriptor);
    }

    private void resolve(EngineDiscoveryRequest request, TestDescriptor engineDescriptor, ClassFilter packageFilter) {
        FeatureResolver featureResolver = FeatureResolver.createFeatureResolver(engineDescriptor, packageFilter);

        request.getSelectorsByType(ClasspathRootSelector.class).forEach(featureResolver::resolveClasspathRoot);
        request.getSelectorsByType(ClasspathResourceSelector.class).forEach(featureResolver::resolveClasspathResource);
        request.getSelectorsByType(PackageSelector.class).forEach(featureResolver::resolvePackageResource);
        request.getSelectorsByType(FileSelector.class).forEach(featureResolver::resolveFile);
        request.getSelectorsByType(DirectorySelector.class).forEach(featureResolver::resolveDirectory);
        request.getSelectorsByType(UniqueIdSelector.class).forEach(featureResolver::resolveUniqueId);
        request.getSelectorsByType(UriSelector.class).forEach(featureResolver::resolveUri);
    }

    private void filter(TestDescriptor engineDescriptor, ClassFilter packageFilter) {
        new DiscoveryFilterApplier().applyPackagePredicate(packageFilter, engineDescriptor);
    }

    private void pruneTree(TestDescriptor rootDescriptor) {
        rootDescriptor.accept(TestDescriptor::prune);
    }

}
