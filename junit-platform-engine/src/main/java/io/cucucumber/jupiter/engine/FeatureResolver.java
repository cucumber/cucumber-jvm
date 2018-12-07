package io.cucucumber.jupiter.engine;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.ClasspathRootResourceLoader;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
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

import static io.cucucumber.jupiter.engine.Classloaders.getDefaultClassLoader;
import static io.cucucumber.jupiter.engine.FeatureOrigin.fromClassPathResource;
import static io.cucucumber.jupiter.engine.FeatureOrigin.fromFileResource;
import static java.util.Collections.singletonList;

final class FeatureResolver {

    private final TestDescriptor engineDescriptor;

    private FeatureResolver(TestDescriptor engineDescriptor) {
        this.engineDescriptor = engineDescriptor;
    }

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor) {
        return new FeatureResolver(engineDescriptor);
    }

    void resolveFile(DirectorySelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getRawPath()))
            .forEach(feature -> resolveFeature(feature, fromFileResource()));
    }

    void resolveFile(FileSelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getRawPath()))
            .forEach(feature -> resolveFeature(feature, fromFileResource()));
    }

    void resolvePackageResource(PackageSelector selector) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(selector.getPackageName().replace('.', '/')))
            .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
    }

    void resolveClassPathResource(ClasspathResourceSelector selector) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(selector.getClasspathResourceName()))
            .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
    }

    void resolveClassPathRoot(ClasspathRootSelector selector) {
        new FeatureLoader(new ClasspathRootResourceLoader(selector.getClasspathRoot()))
            .load(singletonList(""))
            .forEach(feature -> resolveFeature(feature, fromClassPathResource()));
    }

    void resolveUniqueId(UniqueIdSelector uniqueIdSelector) {
        UniqueId uniqueId = uniqueIdSelector.getUniqueId();
        // Ignore any ids not from our own engine
        if (!engineDescriptor.getUniqueId().getEngineId().equals(uniqueId.getEngineId())) {
            return;
        }

        uniqueId.getSegments()
            .stream()
            .filter(segment -> "feature".equals(segment.getType()))
            .findFirst()
            .ifPresent(
                segment -> resolveFromSegment(segment)
                    .map(descriptor -> pruneDescription(descriptor, uniqueIdSelector.getUniqueId()))
                    .forEach(descriptor -> recursivelyMerge(descriptor, engineDescriptor))
            );
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
