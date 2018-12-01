package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;

import static io.cucucumber.jupiter.engine.FeatureResolver.createFeatureResolver;

class DiscoverySelectorResolver {

    void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
        resolve(request, engineDescriptor);
        filter(engineDescriptor);
        pruneTree(engineDescriptor);
    }

    private void resolve(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
        FeatureResolver featureResolver = createFeatureResolver(engineDescriptor);

        request.getSelectorsByType(ModuleSelector.class).forEach(selector -> {
            //TODO: Find all features in a module
        });
        request.getSelectorsByType(ClasspathRootSelector.class).forEach(featureResolver::resolveClassPathRoot);
        request.getSelectorsByType(ClasspathResourceSelector.class).forEach(featureResolver::resolveClassPathResource);
        request.getSelectorsByType(PackageSelector.class).forEach(featureResolver::resolvePackageResource);
        request.getSelectorsByType(FileSelector.class).forEach(featureResolver::resolveFile);
        request.getSelectorsByType(DirectorySelector.class).forEach(featureResolver::resolveFile);
        request.getSelectorsByType(UniqueIdSelector.class).forEach(selector -> {
            //TODO: Find by unique id
        });
        request.getSelectorsByType(UriSelector.class).forEach(featureResolver::resolveUri);
    }

    private void filter(TestDescriptor engineDescriptor) {

    }

    private void pruneTree(TestDescriptor rootDescriptor) {
        rootDescriptor.accept(TestDescriptor::prune);
    }


}
