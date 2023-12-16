package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.ResourceScanner;
import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector;
import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureWithLinesSelector;
import io.cucumber.junit.platform.engine.NodeDescriptor.ExamplesDescriptor;
import io.cucumber.junit.platform.engine.NodeDescriptor.PickleDescriptor;
import io.cucumber.junit.platform.engine.NodeDescriptor.RuleDescriptor;
import io.cucumber.junit.platform.engine.NodeDescriptor.ScenarioOutlineDescriptor;
import io.cucumber.plugin.event.Node;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoverySelector;
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
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElement;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElementAt;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElementsOf;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectFeature;
import static java.util.stream.Collectors.toSet;

final class FeatureResolver implements SelectorResolver {

    private static final Logger log = LoggerFactory.getLogger(FeatureResolver.class);

    private final CachingFeatureParser featureParser = new CachingFeatureParser(new FeatureParser(UUID::randomUUID));
    private final ResourceScanner<Feature> featureScanner = new ResourceScanner<>(
        ClassLoaders::getDefaultClassLoader,
        FeatureIdentifier::isFeature,
        featureParser::parseResource);

    private final Predicate<String> packageFilter;
    private final ConfigurationParameters parameters;
    private final NamingStrategy namingStrategy;

    FeatureResolver(
            ConfigurationParameters parameters,
            Predicate<String> packageFilter
    ) {
        this.parameters = parameters;
        this.packageFilter = packageFilter;
        this.namingStrategy = new CucumberEngineOptions(parameters).namingStrategy();
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

    @Override
    public Resolution resolve(ClasspathResourceSelector selector, Context context) {
        Set<DiscoverySelector> selectors = featureScanner
                .scanForClasspathResource(selector.getClasspathResourceName(), packageFilter)
                .stream()
                .map(feature -> selector.getPosition()
                        .map(position -> selectElementAt(feature, position))
                        .orElseGet(() -> Optional.of(selectFeature(feature))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
        return toResolution(selectors);
    }

    @Override
    public Resolution resolve(UriSelector selector, Context context) {
        URI uri = selector.getUri();
        Set<DiscoverySelector> selectors = Collections.singleton(FeatureWithLinesSelector.from(uri));
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

    @Override
    public Resolution resolve(PackageSelector selector, Context context) {
        return resolvePackageResource(selector.getPackageName());
    }

    private Resolution resolvePackageResource(String packageName) {
        Set<DiscoverySelector> selectors = featureScanner
                .scanForResourcesInPackage(packageName, packageFilter)
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
            // We know now the intention is to run feature files in the
            // package of the annotated class.
            return resolvePackageResourceWarnIfNone(javaClass.getPackage().getName());
        }
        return Resolution.unresolved();
    }

    private Resolution resolvePackageResourceWarnIfNone(String packageName) {
        Resolution resolution = resolvePackageResource(packageName);
        if (resolution.getMatches().isEmpty()) {
            log.warn(() -> "No features found in package '" + packageName + "'");
        }
        return resolution;
    }

    @Override
    public Resolution resolve(ClasspathRootSelector selector, Context context) {
        Set<DiscoverySelector> selectors = featureScanner
                .scanForResourcesInClasspathRoot(selector.getClasspathRoot(), packageFilter)
                .stream()
                .map(FeatureElementSelector::selectFeature)
                .collect(toSet());
        return toResolution(selectors);
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
            if (node instanceof Node.Feature) {
                return Optional.of(new FeatureDescriptor(
                    source.featureSegment(parent.getUniqueId(), feature),
                    namingStrategy.name(node),
                    source.featureSource(),
                    feature));
            }

            if (node instanceof Node.Rule) {
                return Optional.of(new RuleDescriptor(
                    parameters,
                    source.ruleSegment(parent.getUniqueId(), node),
                    namingStrategy.name(node),
                    source.nodeSource(node)));
            }

            if (node instanceof Node.Scenario) {
                return Optional.of(new PickleDescriptor(
                    parameters,
                    source.scenarioSegment(parent.getUniqueId(), node),
                    namingStrategy.name(node),
                    source.nodeSource(node),
                    feature.getPickleAt(node)));
            }

            if (node instanceof Node.ScenarioOutline) {
                return Optional.of(new ScenarioOutlineDescriptor(
                    parameters,
                    source.scenarioSegment(parent.getUniqueId(), node),
                    namingStrategy.name(node),
                    source.nodeSource(node)));
            }

            if (node instanceof Node.Examples) {
                return Optional.of(new ExamplesDescriptor(
                    parameters,
                    source.examplesSegment(parent.getUniqueId(), node),
                    namingStrategy.name(node),
                    source.nodeSource(node)));
            }

            if (node instanceof Node.Example) {
                return Optional.of(new PickleDescriptor(
                    parameters,
                    source.exampleSegment(parent.getUniqueId(), node),
                    namingStrategy.name(node),
                    source.nodeSource(node),
                    feature.getPickleAt(node)));
            }
            return Optional.empty();
        };
    }

    private static Resolution toResolution(Set<? extends DiscoverySelector> selectors) {
        if (selectors.isEmpty()) {
            return Resolution.unresolved();
        }
        return Resolution.selectors(selectors);
    }
}
