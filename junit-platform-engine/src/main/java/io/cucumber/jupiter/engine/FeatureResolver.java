package io.cucumber.jupiter.engine;

import io.cucumber.core.io.Classpath;
import io.cucumber.jupiter.engine.resource.ClassFilter;
import io.cucumber.jupiter.engine.resource.ResourceScanner;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.io.Resource;
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
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static io.cucumber.jupiter.engine.resource.Resources.createClasspathResource;
import static io.cucumber.jupiter.engine.resource.Resources.createClasspathRootResource;
import static io.cucumber.jupiter.engine.resource.Resources.createPackageResource;
import static io.cucumber.jupiter.engine.resource.Resources.createUriResource;
import static java.lang.String.format;
import static java.util.Optional.of;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

final class FeatureResolver {
    private static final String FEATURE_FILE_SUFFIX = ".feature";

    private static final Logger logger = LoggerFactory.getLogger(FeatureResolver.class);

    private final TestDescriptor engineDescriptor;
    private final ClassFilter packageFilter;

    private FeatureResolver(TestDescriptor engineDescriptor, ClassFilter packageFilter) {
        this.engineDescriptor = engineDescriptor;
        this.packageFilter = packageFilter;
    }

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor, ClassFilter packageFilter) {
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

    private static ResourceScanner<CucumberFeature> scanner(BiFunction<Path, Path, Resource> createResource) {
        return new ResourceScanner<>(
            ClassLoaders::getDefaultClassLoader,
            FeatureResolver::isFeature,
            (baseDir, resource) -> {
                Resource specificResource = createResource.apply(baseDir, resource);
                CucumberFeature parsedFeatured = FeatureParser.parseResource(specificResource);
                return of(parsedFeatured);
            }
        );
    }

    private static boolean isFeature(Path path) {
        return path.getFileName().toString().endsWith(FEATURE_FILE_SUFFIX);
    }

    void resolveDirectory(DirectorySelector selector) {
        try {
            resolvePath(selector.getPath());
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in directory '%s'.", selector.getRawPath()));
        }
    }

    private void resolvePath(Path path) {
        scanner(createUriResource())
            .scanForResourcesPath(path)
            .stream()
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    private void merge(TestDescriptor featureDescriptor) {
        recursivelyMerge(featureDescriptor, engineDescriptor);
    }

    void resolveFile(FileSelector selector) {
        try {
            resolvePath(selector.getPath());
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in file '%s'.", selector.getRawPath()));
        }
    }

    void resolvePackageResource(PackageSelector selector) {
        String packageName = selector.getPackageName();
        try {
            scanner(createPackageResource(packageName))
                .scanForResourcesInPackage(packageName, packageFilter::match)
                .stream()
                .map(this::resolveFeature)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve features in package '%s'.", packageName));
        }
    }

    void resolveClasspathResource(ClasspathResourceSelector selector) {
        String classpathResourceName = selector.getClasspathResourceName();
        try {
            scanner(createClasspathResource(classpathResourceName))
                .scanForClasspathResource(classpathResourceName, packageFilter::match)
                .stream()
                .map(this::resolveFeature)
                .forEach(this::merge);
        } catch (Throwable e) {
            rethrowIfBlacklisted(e);
            logger.debug(e, () -> format("Failed to resolve feature '%s'.", classpathResourceName));
        }
    }

    void resolveClasspathRoot(ClasspathRootSelector selector) {
        try {
            scanner(createClasspathRootResource())
                .scanForResourcesInClasspathRoot(selector.getClasspathRoot(), packageFilter::match)
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
        if (FeatureOrigin.isClassPath(uri)) {
            String resourcePath = Classpath.resourceName(uri);
            testDescriptorStream = scanner(createClasspathResource(resourcePath))
                .scanForClasspathResource(resourcePath, packageFilter::match);
        } else {
            testDescriptorStream = scanner(createUriResource())
                .scanForResourcesUri(uri);
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
                .forEach(this::merge);
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
