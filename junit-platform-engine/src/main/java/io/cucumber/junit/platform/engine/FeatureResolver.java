package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.ResourceScanner;
import io.cucumber.plugin.event.Node;
import org.junit.platform.engine.ConfigurationParameters;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

    private final CucumberEngineDescriptor engineDescriptor;
    private final Predicate<String> packageFilter;
    private final ConfigurationParameters parameters;

    private FeatureResolver(ConfigurationParameters parameters, CucumberEngineDescriptor engineDescriptor, Predicate<String> packageFilter) {
        this.parameters = parameters;
        this.engineDescriptor = engineDescriptor;
        this.packageFilter = packageFilter;
    }

    static FeatureResolver createFeatureResolver(ConfigurationParameters parameters, CucumberEngineDescriptor engineDescriptor, Predicate<String> packageFilter) {
        return new FeatureResolver(parameters, engineDescriptor, packageFilter);
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
            .map(this::createFeatureDescriptor)
            .forEach(engineDescriptor::mergeFeature);
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
            .map(this::createFeatureDescriptor)
            .forEach(engineDescriptor::mergeFeature);
    }

    void resolveClasspathResource(ClasspathResourceSelector selector) {
        String classpathResourceName = selector.getClasspathResourceName();
        featureScanner
            .scanForClasspathResource(classpathResourceName, packageFilter)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::createFeatureDescriptor)
            .forEach(engineDescriptor::mergeFeature);
    }

    void resolveClasspathRoot(ClasspathRootSelector selector) {
        featureScanner
            .scanForResourcesInClasspathRoot(selector.getClasspathRoot(), packageFilter)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::createFeatureDescriptor)
            .forEach(engineDescriptor::mergeFeature);
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
            .forEach(featureDescriptor -> {
                featureDescriptor.prune(keepTestWithSelectedId);
                engineDescriptor.mergeFeature(featureDescriptor);
            });
    }

    void resolveUri(UriSelector selector) {
        URI uri = selector.getUri();

        Predicate<TestDescriptor> keepTestOnSelectedLine = fromQuery(uri.getQuery())
            .map(FilePosition::getLine)
            .map(FeatureResolver::testDescriptorOnLine)
            .orElse(testDescriptor -> true);

        resolveUri(stripQuery(uri))
            .forEach(featureDescriptor -> {
                featureDescriptor.prune(keepTestOnSelectedLine);
                engineDescriptor.mergeFeature(featureDescriptor);
            });
    }

    private Stream<FeatureDescriptor> resolveUri(URI uri) {
        return featureScanner
            .scanForResourcesUri(uri)
            .stream()
            .sorted(comparing(Feature::getUri))
            .map(this::createFeatureDescriptor);
    }

    private FeatureDescriptor createFeatureDescriptor(Feature feature) {
        FeatureOrigin source = FeatureOrigin.fromUri(feature.getUri());

        return (FeatureDescriptor) feature.map(
            engineDescriptor,
            (Node.Feature self, TestDescriptor parent) -> new FeatureDescriptor(
                source.featureSegment(parent.getUniqueId(), feature),
                getNameOrKeyWord(self),
                source.featureSource(),
                feature
            ),
            (Node.Rule node, TestDescriptor parent) -> {
                TestDescriptor descriptor = new NodeDescriptor(
                    source.ruleSegment(parent.getUniqueId(), node),
                    getNameOrKeyWord(node),
                    source.nodeSource(node)
                );
                parent.addChild(descriptor);
                return descriptor;
            }, (Node.Scenario node, TestDescriptor parent) -> {
                Pickle pickle = feature.getPickleAt(node);
                TestDescriptor descriptor = new PickleDescriptor(
                    parameters,
                    source.scenarioSegment(parent.getUniqueId(), node),
                    getNameOrKeyWord(node),
                    source.nodeSource(node),
                    pickle
                );
                parent.addChild(descriptor);
                return descriptor;
            },
            (Node.ScenarioOutline node, TestDescriptor parent) -> {
                TestDescriptor descriptor = new NodeDescriptor(
                    source.scenarioSegment(parent.getUniqueId(), node),
                    getNameOrKeyWord(node),
                    source.nodeSource(node)
                );
                parent.addChild(descriptor);
                return descriptor;
            },
            (Node.Examples node, TestDescriptor parent) -> {
                NodeDescriptor descriptor = new NodeDescriptor(
                    source.examplesSegment(parent.getUniqueId(), node),
                    getNameOrKeyWord(node),
                    source.nodeSource(node)
                );
                parent.addChild(descriptor);
                return descriptor;
            },
            (Node.Example node, TestDescriptor parent) -> {
                Pickle pickle = feature.getPickleAt(node);
                PickleDescriptor descriptor = new PickleDescriptor(
                    parameters,
                    source.exampleSegment(parent.getUniqueId(), node),
                    getNameOrKeyWord(node),
                    source.nodeSource(node),
                    pickle
                );
                parent.addChild(descriptor);
                return descriptor;
            }
        );
    }

    private String getNameOrKeyWord(Node node) {
        Supplier<String> keyword = () -> node.getKeyWord().orElse("Unknown");
        return node.getName().orElseGet(keyword);
    }

}
