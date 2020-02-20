package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.ResourceScanner;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.junit.platform.engine.support.descriptor.FilePosition.fromQuery;

final class FeatureResolver {

    private final FeatureParser featureParser = new FeatureParser(UUID::randomUUID);
    private final ResourceScanner<Feature> featureScanner = new ResourceScanner<>(
        ClassLoaders::getDefaultClassLoader,
        FeatureIdentifier::isFeature,
        featureParser::parseResource
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

    private static URI stripQuery(URI uri) {
        if (uri.getQuery() == null) {
            return uri;
        }
        String uriString = uri.toString();
        return URI.create(uriString.substring(0, uriString.indexOf('?')));
    }

    private static Predicate<TestDescriptor> testDescriptorOnLine(Integer line) {
        return descriptor -> descriptor.getSource()
            .flatMap(testSource -> {
                if (testSource instanceof FileSource) {
                    FileSource fileSystemSource = (FileSource) testSource;
                    return fileSystemSource.getPosition();
                }
                if (testSource instanceof ClasspathResourceSource) {
                    ClasspathResourceSource classpathResourceSource = (ClasspathResourceSource) testSource;
                    return classpathResourceSource.getPosition();
                }
                return Optional.empty();
            })
            .map(FilePosition::getLine)
            .map(line::equals)
            .orElse(false);
    }

    private static Function<TestDescriptor, TestDescriptor> pruneDescriptions(Predicate<TestDescriptor> toKeep) {
        return descriptor -> {
            pruneDescriptionRecursively(descriptor, toKeep);
            return descriptor;
        };
    }

    private static void pruneDescriptionRecursively(TestDescriptor descriptor, Predicate<TestDescriptor> toKeep) {
        if (toKeep.test(descriptor)) {
            return;
        }

        if (descriptor.isTest()) {
            descriptor.removeFromHierarchy();
        }

        List<TestDescriptor> children = new ArrayList<>(descriptor.getChildren());
        children.forEach(child -> pruneDescriptionRecursively(child, toKeep));
    }

    private void merge(TestDescriptor featureDescriptor) {
        recursivelyMerge(featureDescriptor, engineDescriptor);
    }

    void resolveFile(FileSelector selector) {
        resolvePath(selector.getPath());
    }

    void resolveDirectory(DirectorySelector selector) {
        resolvePath(selector.getPath());
    }

    private void resolvePath(Path path) {
        featureScanner
            .scanForResourcesPath(path)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    void resolvePackageResource(PackageSelector selector) {
        resolvePackageResource(selector.getPackageName());
    }

    void resolveClass(ClassSelector classSelector) {
        Class<?> javaClass = classSelector.getJavaClass();
        Cucumber annotation = javaClass.getAnnotation(Cucumber.class);
        if (annotation != null) {
            resolvePackageResource(javaClass.getPackage().getName());
        }
    }

    private void resolvePackageResource(String packageName) {
        featureScanner
            .scanForResourcesInPackage(packageName, packageFilter)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    void resolveClasspathResource(ClasspathResourceSelector selector) {
        String classpathResourceName = selector.getClasspathResourceName();
        featureScanner
            .scanForClasspathResource(classpathResourceName, packageFilter)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    void resolveClasspathRoot(ClasspathRootSelector selector) {
        featureScanner
            .scanForResourcesInClasspathRoot(selector.getClasspathRoot(), packageFilter)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::resolveFeature)
            .forEach(this::merge);
    }

    void resolveUniqueId(UniqueIdSelector uniqueIdSelector) {
        UniqueId uniqueId = uniqueIdSelector.getUniqueId();
        // Ignore any ids not from our own engine
        if (!engineDescriptor.getUniqueId().getEngineId().equals(uniqueId.getEngineId())) {
            return;
        }

        Predicate<TestDescriptor> keepTestWithSelectedId = testDescriptor
            -> uniqueId.equals(testDescriptor.getUniqueId());

        uniqueId.getSegments()
            .stream()
            .filter(FeatureOrigin::isFeatureSegment)
            .map(UniqueId.Segment::getValue)
            .map(URI::create)
            .flatMap(this::resolveUri)
            .map(pruneDescriptions(keepTestWithSelectedId))
            .forEach(this::merge);
    }

    void resolveUri(UriSelector selector) {
        URI uri = selector.getUri();

        Predicate<TestDescriptor> keepTestOnSelectedLine = fromQuery(uri.getQuery())
            .map(FilePosition::getLine)
            .map(FeatureResolver::testDescriptorOnLine)
            .orElse(testDescriptor -> true);

        resolveUri(stripQuery(uri))
            .map(pruneDescriptions(keepTestOnSelectedLine))
            .forEach(this::merge);
    }

    private Stream<TestDescriptor> resolveUri(URI uri) {
        return featureScanner
            .scanForResourcesUri(uri)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::resolveFeature);
    }

    private TestDescriptor resolveFeature(Feature feature) {
        return FeatureDescriptor.create(feature, engineDescriptor);
    }

}
