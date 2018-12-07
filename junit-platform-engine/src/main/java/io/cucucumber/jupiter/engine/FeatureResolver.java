package io.cucucumber.jupiter.engine;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.ClasspathRootResourceLoader;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.cucucumber.jupiter.engine.ClassLoaders.getDefaultClassLoader;
import static io.cucucumber.jupiter.engine.FeatureOrigin.fromClassPathResource;
import static io.cucucumber.jupiter.engine.FeatureOrigin.fromFileResource;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

final class FeatureResolver {

    private static final Logger logger = LoggerFactory.getLogger(FeatureResolver.class);

    private final TestDescriptor engineDescriptor;

    private FeatureResolver(TestDescriptor engineDescriptor) {
        this.engineDescriptor = engineDescriptor;
    }

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor) {
        return new FeatureResolver(engineDescriptor);
    }

    void resolveFile(DirectorySelector selector) {
        try {
            new FeatureLoader(new FileResourceLoader())
                .load(singletonList(selector.getRawPath()))
                .forEach(feature -> resolveFeature(feature, fromFileResource()));
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in directory '%s'.", selector.getRawPath()));
        }
    }

    void resolveFile(FileSelector selector) {
        try {
            new FeatureLoader(new FileResourceLoader())
                .load(singletonList(selector.getRawPath()))
                .forEach(feature -> resolveFeature(feature, fromFileResource()));
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in file '%s'.", selector.getRawPath()));
        }
    }

    void resolvePackageResource(PackageSelector selector) {
        try {
            new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
                .load(singletonList(selector.getPackageName().replace('.', '/')))
                .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in package '%s'.", selector.getPackageName()));
        }
    }

    void resolveClassPathResource(ClasspathResourceSelector selector) {
        try {
            new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
                .load(singletonList(selector.getClasspathResourceName()))
                .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in classpath '%s'.", selector.getClasspathResourceName()));
        }
    }

    void resolveClassPathRoot(ClasspathRootSelector selector) {
        try {
            new FeatureLoader(new ClasspathRootResourceLoader(selector.getClasspathRoot()))
                .load(singletonList(""))
                .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in classpath root '%s'.", selector.getClasspathRoot()));
        }
    }

    void resolveUniqueId(UniqueIdSelector uniqueIdSelector) {
        UniqueId uniqueId = uniqueIdSelector.getUniqueId();
        // Ignore any ids not from our own engine
        if (!engineDescriptor.getUniqueId().getEngineId().equals(uniqueId.getEngineId())) {
            return;
        }

        try {
            uniqueId.getSegments()
                .stream()
                .filter(segment -> "feature".equals(segment.getType()))
                .findFirst()
                .ifPresent(
                    segment -> resolveFromSegment(segment)
                        .map(descriptor -> pruneDescription(descriptor, uniqueIdSelector.getUniqueId()))
                        .forEach(descriptor -> recursivelyMerge(descriptor, engineDescriptor))
                );
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features for '%s'.", uniqueIdSelector.getUniqueId()));
        }
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

    private Stream<TestDescriptor> resolveFromSegment(UniqueId.Segment segment) {
        FeatureOrigin origin = FeatureOrigin.fromSegment(segment);


        return new FeatureLoader(new MultiLoader(getDefaultClassLoader()))
            .load(singletonList(origin.toFeaturePath(segment)))
            .stream()
            .map(feature -> FeatureDescriptor.create(feature, origin, engineDescriptor));
    }

    private void resolveFeature(CucumberFeature feature, FeatureOrigin source) {
        TestDescriptor featureDescriptor = FeatureDescriptor.create(feature, source, engineDescriptor);
        recursivelyMerge(featureDescriptor, engineDescriptor);
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
}
