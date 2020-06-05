package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;

import java.util.function.Predicate;

import static io.cucumber.junit.platform.engine.FeatureResolver.createFeatureResolver;
import static org.junit.platform.engine.Filter.composeFilters;

class DiscoverySelectorResolver {

    void resolveSelectors(EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor) {
        Predicate<String> packageFilter = buildPackageFilter(request);
        resolve(request, engineDescriptor, packageFilter);
        filter(engineDescriptor, packageFilter);
        pruneTree(engineDescriptor);
    }

    private Predicate<String> buildPackageFilter(EngineDiscoveryRequest request) {
        Filter<String> packageFilter = composeFilters(request.getFiltersByType(PackageNameFilter.class));
        return packageFilter.toPredicate();
    }

    private void resolve(
            EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor, Predicate<String> packageFilter
    ) {
        FeatureResolver featureResolver = createFeatureResolver(request.getConfigurationParameters(), engineDescriptor,
            packageFilter);

        request.getSelectorsByType(ClasspathRootSelector.class).forEach(featureResolver::resolveClasspathRoot);
        request.getSelectorsByType(ClasspathResourceSelector.class).forEach(featureResolver::resolveClasspathResource);
        request.getSelectorsByType(ClassSelector.class).forEach(featureResolver::resolveClass);
        request.getSelectorsByType(PackageSelector.class).forEach(featureResolver::resolvePackageResource);
        request.getSelectorsByType(FileSelector.class).forEach(featureResolver::resolveFile);
        request.getSelectorsByType(DirectorySelector.class).forEach(featureResolver::resolveDirectory);
        request.getSelectorsByType(UniqueIdSelector.class).forEach(featureResolver::resolveUniqueId);
        request.getSelectorsByType(UriSelector.class).forEach(featureResolver::resolveUri);
    }

    private void filter(TestDescriptor engineDescriptor, Predicate<String> packageFilter) {
        applyPackagePredicate(packageFilter, engineDescriptor);
    }

    private void pruneTree(TestDescriptor rootDescriptor) {
        rootDescriptor.accept(TestDescriptor::prune);
    }

    private void applyPackagePredicate(Predicate<String> packageFilter, TestDescriptor engineDescriptor) {
        engineDescriptor.accept(descriptor -> {
            if (descriptor instanceof PickleDescriptor) {
                PickleDescriptor pickleDescriptor = (PickleDescriptor) descriptor;
                if (!includePickle(pickleDescriptor, packageFilter)) {
                    descriptor.removeFromHierarchy();
                }
            }
        });
    }

    private boolean includePickle(PickleDescriptor pickleDescriptor, Predicate<String> packageFilter) {
        return pickleDescriptor.getPackage()
                .map(packageFilter::test)
                .orElse(true);
    }

}
