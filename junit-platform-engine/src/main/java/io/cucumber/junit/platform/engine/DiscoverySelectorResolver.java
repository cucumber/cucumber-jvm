package io.cucumber.junit.platform.engine;

import io.cucumber.core.resource.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;

import static org.junit.platform.engine.Filter.composeFilters;

class DiscoverySelectorResolver {

    static ClassFilter buildPackageFilter(EngineDiscoveryRequest request) {
        Filter<String> packageFilter = composeFilters(request.getFiltersByType(PackageNameFilter.class));
        return ClassFilter.of(packageFilter.toPredicate(), aClass -> true);
    }

    void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
        ClassFilter packageFilter = buildPackageFilter(request);
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
        applyPackagePredicate(packageFilter, engineDescriptor);
    }

    private void applyPackagePredicate(ClassFilter packagePredicate, TestDescriptor engineDescriptor) {
        engineDescriptor.accept(descriptor -> {
            if (descriptor instanceof PickleDescriptor
                && !includePickle((PickleDescriptor) descriptor, packagePredicate)) {
                descriptor.removeFromHierarchy();
            }
        });
    }

    private boolean includePickle(PickleDescriptor pickleDescriptor, ClassFilter packagePredicate) {
        return pickleDescriptor.getPackage()
            .map(packagePredicate::match)
            .orElse(true);
    }

    private void pruneTree(TestDescriptor rootDescriptor) {
        rootDescriptor.accept(TestDescriptor::prune);
    }

}
