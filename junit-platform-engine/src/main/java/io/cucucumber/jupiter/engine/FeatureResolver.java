package io.cucucumber.jupiter.engine;

import cucumber.runtime.model.CucumberFeature;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassFilter;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static cucumber.runtime.model.FeatureParser.parseResource;
import static io.cucucumber.jupiter.engine.FeatureOrigin.isClassPath;
import static io.cucucumber.jupiter.engine.Resources.createClasspathResource;
import static io.cucucumber.jupiter.engine.Resources.createClasspathRootResource;
import static io.cucucumber.jupiter.engine.Resources.createPackageResource;
import static io.cucucumber.jupiter.engine.Resources.createUriResource;
import static java.lang.String.format;
import static java.util.Optional.of;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

final class FeatureResolver {
    private static final String FEATURE_FILE_SUFFIX = ".feature";

    private static final Logger logger = LoggerFactory.getLogger(FeatureResolver.class);

    private final ResourceScanner<CucumberFeature> uriResourceScanner = new ResourceScanner<>(
        ClassLoaders::getDefaultClassLoader,
        this::isFeature,
        (baseDir, resource) -> of(parseResource(createUriResource(resource)))
    );
    private final ResourceScanner<CucumberFeature> classpathFeatureScanner = new ResourceScanner<>(
        ClassLoaders::getDefaultClassLoader,
        this::isFeature,
        (baseDir, resource) -> of(parseResource(createClasspathRootResource(baseDir, resource)))
    );
    private final TestDescriptor engineDescriptor;
    private final ClassFilter filter;

    private FeatureResolver(TestDescriptor engineDescriptor, ClassFilter filter) {
        this.engineDescriptor = engineDescriptor;
        this.filter = filter;
    }

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor, ClassFilter filter) {
        return new FeatureResolver(engineDescriptor, filter);
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

    private boolean isFeature(Path path) {
        return path.getFileName().toString().endsWith(FEATURE_FILE_SUFFIX);
    }

    void resolveDirectory(DirectorySelector selector) {
        try {
            uriResourceScanner
                .scanForResourcesPath(selector.getPath())
                .stream()
                .map(this::resolveFeature)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in directory '%s'.", selector.getRawPath()));
        }
    }

    private void merge(TestDescriptor featureDescriptor) {
        mergeWithParent(featureDescriptor, engineDescriptor);
    }

    private void mergeWithParent(TestDescriptor featureDescriptor, TestDescriptor engineDescriptor) {
        recursivelyMerge(featureDescriptor, engineDescriptor);
    }

    void resolveFile(FileSelector selector) {
        try {
            uriResourceScanner
                .scanForResourcesPath(selector.getPath())
                .stream()
                .map(this::resolveFeature)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in file '%s'.", selector.getRawPath()));
        }
    }

    void resolvePackageResource(PackageSelector selector) {
        try {
            final ResourceScanner<CucumberFeature> packageScanner = new ResourceScanner<>(
                ClassLoaders::getDefaultClassLoader,
                this::isFeature,
                (baseDir, resource) -> of(parseResource(createPackageResource(baseDir, selector.getPackageName(), resource)))
            );

            packageScanner
                .scanForResourcesInPackage(selector.getPackageName(), filter::match)
                .stream()
                .map(this::resolveFeature)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in package '%s'.", selector.getPackageName()));
        }
    }

    void resolveClasspathResource(ClasspathResourceSelector selector) {
        try {
            final ResourceScanner<CucumberFeature> packageScanner = new ResourceScanner<>(
                ClassLoaders::getDefaultClassLoader,
                this::isFeature,
                (baseDir, resource) -> of(parseResource(createClasspathResource(selector.getClasspathResourceName(), resource)))
            );

            packageScanner
                .scanForClasspathResource(selector.getClasspathResourceName(), filter::match)
                .stream()
                .map(this::resolveFeature)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve feature '%s'.", selector.getClasspathResourceName()));
        }
    }

    void resolveClasspathRoot(ClasspathRootSelector selector) {
        try {
            classpathFeatureScanner
                .scanForResourcesInClasspathRoot(selector.getClasspathRoot(), filter::match)
                .stream()
                .map(this::resolveFeature)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in classpath root '%s'.", selector.getClasspathRoot()));
        }
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
        List<CucumberFeature> testDescriptorStream;
        if (isClassPath(uri)) {
            testDescriptorStream = uriResourceScanner.scanForResourcesPath(Paths.get(uri));
        } else {
            testDescriptorStream = uriResourceScanner.scanForResourcesInClasspathRoot(uri, filter::match);
        }
        return testDescriptorStream
            .stream()
            .map(this::resolveFeature);
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
                .filter(FeatureOrigin::isFeatureSegment)
                .map(UniqueId.Segment::getValue)
                .map(URI::create)
                .flatMap(this::resolveUri)
                .map(descriptor -> pruneDescription(descriptor, uniqueIdSelector.getUniqueId()))
                .forEach(descriptor -> recursivelyMerge(descriptor, engineDescriptor));
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

    private TestDescriptor resolveFeature(CucumberFeature feature) {
        return FeatureDescriptor.create(feature, engineDescriptor);
    }

}
