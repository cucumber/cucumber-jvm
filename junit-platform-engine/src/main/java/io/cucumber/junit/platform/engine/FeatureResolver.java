package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.ResourceScanner;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.cucumber.core.feature.FeatureParser.parseResource;
import static java.lang.String.format;
import static java.util.Optional.of;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

final class FeatureResolver {

    private static final Logger logger = LoggerFactory.getLogger(FeatureResolver.class);
    private final ResourceScanner<CucumberFeature> featureScanner = new ResourceScanner<>(
        ClassLoaders::getDefaultClassLoader,
        FeatureIdentifier::isFeature,
        resource -> of(parseResource(resource))
    );

    private final TestDescriptor engineDescriptor;
    private final Predicate<String> packageFilter;

    private FeatureResolver(TestDescriptor engineDescriptor, Predicate<String> packageFilter) {
        this.engineDescriptor = engineDescriptor;
        this.packageFilter = packageFilter;
    }

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor, Predicate<String> packageFilter) {
        return new FeatureResolver(engineDescriptor, packageFilter);
    }

    private static void recursivelyMerge(TestDescriptor descriptor, TestDescriptor parent) {
        Optional<? extends TestDescriptor> byUniqueId = parent.findByUniqueId(descriptor.getUniqueId());
        if (!byUniqueId.isPresent()) {
            parent.addChild(descriptor);
            return;
        }

        byUniqueId.ifPresent(
            existingParent -> descriptor.getChildren()
                .forEach(child -> recursivelyMerge(child, existingParent))
        );
    }

    void resolveDirectory(DirectorySelector selector) {
        resolvePath(selector.getPath());
    }

    private void resolvePath(Path path) {
        featureScanner
            .scanForResourcesPath(path)
            .stream()
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    private void merge(TestDescriptor featureDescriptor) {
        recursivelyMerge(featureDescriptor, engineDescriptor);
    }

    void resolveFile(FileSelector selector) {
        resolvePath(selector.getPath());
    }

    void resolvePackageResource(PackageSelector selector) {
        String packageName = selector.getPackageName();
        featureScanner
            .scanForResourcesInPackage(packageName, packageFilter)
            .stream()
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    void resolveClasspathResource(ClasspathResourceSelector selector) {
        String classpathResourceName = selector.getClasspathResourceName();
        featureScanner
            .scanForClasspathResource(classpathResourceName, packageFilter)
            .stream()
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    void resolveClasspathRoot(ClasspathRootSelector selector) {
        featureScanner
            .scanForResourcesInClasspathRoot(selector.getClasspathRoot(), packageFilter)
            .stream()
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    void resolveUri(UriSelector selector) {
        URI uri = selector.getUri();

        try {
            resolveUri(uri)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features for uri '%s'.", uri));
        }
    }

    private Stream<TestDescriptor> resolveUri(URI uri) {
        return featureScanner
            .scanForResourcesUri(uri)
            .stream()
            .map(this::resolveFeature);
    }

    void resolveUniqueId(UniqueIdSelector uniqueIdSelector) {
        UniqueId uniqueId = uniqueIdSelector.getUniqueId();
        // Ignore any ids not from our own engine
        if (!engineDescriptor.getUniqueId().getEngineId().equals(uniqueId.getEngineId())) {
            return;
        }

        uniqueId.getSegments()
            .stream()
            .filter(FeatureOrigin::isFeatureSegment)
            .map(UniqueId.Segment::getValue)
            .map(URI::create)
            .flatMap(this::resolveUri)
            .map(descriptor -> pruneDescription(descriptor, uniqueIdSelector.getUniqueId()))
            .forEach(this::merge);
    }

    private TestDescriptor pruneDescription(TestDescriptor descriptor, UniqueId toKeep) {
        pruneDescriptionRecursively(descriptor, toKeep);
        return descriptor;
    }

    private void pruneDescriptionRecursively(TestDescriptor descriptor, UniqueId toKeep) {
        if (descriptor.isTest() && !descriptor.getUniqueId().hasPrefix(toKeep)) {
            descriptor.removeFromHierarchy();
            return;
        }

        List<TestDescriptor> children = new ArrayList<>(descriptor.getChildren());
        children.forEach(child -> pruneDescriptionRecursively(child, toKeep));
    }

    private TestDescriptor resolveFeature(CucumberFeature feature) {
        return FeatureDescriptor.create(feature, engineDescriptor);
    }

}
