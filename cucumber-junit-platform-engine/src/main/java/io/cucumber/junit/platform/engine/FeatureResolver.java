package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.ResourceScanner;
import io.cucumber.core.runtime.UuidGeneratorServiceLoader;
import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector;
import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureWithLinesSelector;
import io.cucumber.junit.platform.engine.FeatureElementDescriptor.ExamplesDescriptor;
import io.cucumber.junit.platform.engine.FeatureElementDescriptor.PickleDescriptor;
import io.cucumber.junit.platform.engine.FeatureElementDescriptor.RuleDescriptor;
import io.cucumber.junit.platform.engine.FeatureElementDescriptor.ScenarioOutlineDescriptor;
import io.cucumber.plugin.event.Node;
import org.junit.platform.commons.support.Resource;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElement;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElementAt;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElementsOf;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectFeature;
import static io.cucumber.junit.platform.engine.FeatureOrigin.EXAMPLES_SEGMENT_TYPE;
import static io.cucumber.junit.platform.engine.FeatureOrigin.EXAMPLE_SEGMENT_TYPE;
import static io.cucumber.junit.platform.engine.FeatureOrigin.FEATURE_SEGMENT_TYPE;
import static io.cucumber.junit.platform.engine.FeatureOrigin.RULE_SEGMENT_TYPE;
import static io.cucumber.junit.platform.engine.FeatureOrigin.SCENARIO_SEGMENT_TYPE;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

final class FeatureResolver implements SelectorResolver {

    private final ResourceScanner<Feature> featureScanner;

    private final ConfigurationParameters parameters;
    private final NamingStrategy namingStrategy;
    private final CachingFeatureParser featureParser;

    FeatureResolver(ConfigurationParameters parameters) {
        this.parameters = parameters;
        CucumberEngineOptions options = new CucumberEngineOptions(parameters);
        this.namingStrategy = options.namingStrategy();
        this.featureParser = createFeatureParser(options);
        this.featureScanner = new ResourceScanner<>(
            ClassLoaders::getDefaultClassLoader,
            FeatureIdentifier::isFeature,
            featureParser::parseResource);
    }

    private static CachingFeatureParser createFeatureParser(CucumberEngineOptions options) {
        Supplier<ClassLoader> classLoader = FeatureResolver.class::getClassLoader;
        UuidGeneratorServiceLoader uuidGeneratorServiceLoader = new UuidGeneratorServiceLoader(classLoader, options);
        UuidGenerator uuidGenerator = uuidGeneratorServiceLoader.loadUuidGenerator();
        FeatureParser featureParser = new FeatureParser(uuidGenerator::generateId);
        return new CachingFeatureParser(featureParser);
    }

    @Override
    public Resolution resolve(DiscoverySelector selector, Context context) {
        if (selector instanceof FeatureElementSelector) {
            return resolve((FeatureElementSelector) selector, context);
        }
        if (selector instanceof FeatureWithLinesSelector) {
            return resolve((FeatureWithLinesSelector) selector);
        }
        return SelectorResolver.super.resolve(selector, context);
    }

    public Resolution resolve(FeatureElementSelector selector, Context context) {
        Feature feature = selector.getFeature();
        Node selected = selector.getElement();
        return selected.getParent()
                .map(parent -> context.addToParent(() -> selectElement(feature, parent),
                    createTestDescriptor(feature, selected)))
                .orElseGet(() -> context.addToParent(createTestDescriptor(feature, selected)))
                .map(descriptor -> Match.exact(descriptor, () -> selectElementsOf(feature, selected)))
                .map(Resolution::match)
                .orElseGet(Resolution::unresolved);
    }

    public Resolution resolve(FeatureWithLinesSelector selector) {
        URI uri = selector.getUri();
        Set<DiscoverySelector> selectors = featureScanner
                .scanForResourcesUri(uri)
                .stream()
                .flatMap(feature -> selector.getFilePositions()
                        .map(filePositions -> filePositions.stream()
                                .map(position -> selectElementAt(feature, position))
                                .filter(Optional::isPresent)
                                .map(Optional::get))
                        .orElseGet(() -> Stream.of(selectFeature(feature))))
                .collect(toSet());

        return toResolution(selectors);
    }

    @Override
    public Resolution resolve(FileSelector selector, Context context) {
        Set<DiscoverySelector> selectors = featureScanner
                .scanForResourcesPath(selector.getPath())
                .stream()
                .map(feature -> selector.getPosition()
                        .map(position -> selectElementAt(feature, position))
                        .orElseGet(() -> Optional.of(selectFeature(feature))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
        return toResolution(selectors);
    }

    private final IsFeature isFeature = new IsFeature();

    @Override
    public Resolution resolve(ClasspathResourceSelector selector, Context context) {
        Set<Resource> resources = selector.getClasspathResources();
        if (!resources.stream().allMatch(isFeature)) {
            return Resolution.unresolved();
        }
        if (resources.size() > 1) {
            throw new IllegalArgumentException(String.format(
                "Found %s resources named %s classpath %s. Using the first.",
                resources.size(), selector.getClasspathResourceName(),
                resources.stream().map(Resource::getUri).collect(toList())));
        }
        return resources.stream()
                .findFirst()
                .flatMap(featureParser::parseResource)
                .map(feature -> selector.getPosition()
                        .map(position -> selectElementAt(feature, position))
                        .orElseGet(() -> Optional.of(selectFeature(feature))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Collections::singleton)
                .map(FeatureResolver::toResolution)
                .orElseGet(Resolution::unresolved);
    }

    @Override
    public Resolution resolve(UriSelector selector, Context context) {
        URI uri = selector.getUri();
        Set<DiscoverySelector> selectors = singleton(FeatureWithLinesSelector.from(uri));
        return toResolution(selectors);
    }

    @Override
    public Resolution resolve(DirectorySelector selector, Context context) {
        Set<DiscoverySelector> selectors = featureScanner
                .scanForResourcesPath(selector.getPath())
                .stream()
                .map(FeatureElementSelector::selectFeature)
                .collect(toSet());
        return toResolution(selectors);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        Class<?> javaClass = selector.getJavaClass();
        Cucumber annotation = javaClass.getAnnotation(Cucumber.class);
        if (annotation != null) {
            String packageName = javaClass.getPackage().getName();
            Set<DiscoverySelector> selectors = singleton(selectPackage(packageName));
            return toResolution(selectors);
        }
        return Resolution.unresolved();
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        UniqueId uniqueId = selector.getUniqueId();
        Set<FeatureWithLinesSelector> selectors = FeatureWithLinesSelector.from(uniqueId);
        return toResolution(selectors);
    }

    private Function<TestDescriptor, Optional<TestDescriptor>> createTestDescriptor(Feature feature, Node node) {
        return parent -> {
            FeatureOrigin source = FeatureOrigin.fromUri(feature.getUri());
            String name = namingStrategy.name(node);

            if (node instanceof Node.Feature) {
                return Optional.of(new FeatureDescriptor(
                    parent.getUniqueId().append(FEATURE_SEGMENT_TYPE, feature.getUri().toString()),
                    name,
                    source.featureSource(),
                    feature));
            }

            TestSource testSource = source.nodeSource(node);
            int line = node.getLocation().getLine();

            if (node instanceof Node.Rule) {
                return Optional.of(new RuleDescriptor(
                    parameters,
                    parent.getUniqueId().append(RULE_SEGMENT_TYPE,
                        String.valueOf(line)),
                    name,
                    testSource,
                    line));
            }

            if (node instanceof Node.Scenario) {
                return Optional.of(new PickleDescriptor(
                    parameters,
                    parent.getUniqueId().append(SCENARIO_SEGMENT_TYPE,
                        String.valueOf(line)),
                    name,
                    testSource,
                    line,
                    feature.getPickleAt(node)));
            }

            if (node instanceof Node.ScenarioOutline) {
                return Optional.of(new ScenarioOutlineDescriptor(
                    parameters,
                    parent.getUniqueId().append(SCENARIO_SEGMENT_TYPE,
                        String.valueOf(line)),
                    name,
                    testSource,
                    line));
            }

            if (node instanceof Node.Examples) {
                return Optional.of(new ExamplesDescriptor(
                    parameters,
                    parent.getUniqueId().append(EXAMPLES_SEGMENT_TYPE,
                        String.valueOf(line)),
                    name,
                    testSource,
                    line));
            }

            if (node instanceof Node.Example) {
                Pickle pickle = feature.getPickleAt(node);
                return Optional.of(new PickleDescriptor(
                    parameters,
                    parent.getUniqueId().append(EXAMPLE_SEGMENT_TYPE,
                        String.valueOf(line)),
                    namingStrategy.nameExample(node, pickle),
                    testSource,
                    line,
                    pickle));
            }
            throw new IllegalStateException("Got a " + node.getClass() + " but didn't have a case to handle it");
        };
    }

    private static Resolution toResolution(Set<? extends DiscoverySelector> selectors) {
        if (selectors.isEmpty()) {
            return Resolution.unresolved();
        }
        return Resolution.selectors(selectors);
    }
}
