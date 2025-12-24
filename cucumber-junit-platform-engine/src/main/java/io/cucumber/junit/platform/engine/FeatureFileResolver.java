package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.ResourceScanner;
import io.cucumber.core.runtime.UuidGeneratorServiceLoader;
import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector;
import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureWithLinesSelector;
import io.cucumber.plugin.event.Node;
import org.junit.platform.commons.support.Resource;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.core.feature.FeatureIdentifier.isFeature;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElement;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElementAt;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElementsAt;
import static io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureElementSelector.selectElementsOf;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.engine.DiscoveryIssue.Severity.WARNING;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

final class FeatureFileResolver implements SelectorResolver {
    private final ResourceScanner<FeatureWithSource> featureScanner;

    private final FeatureParserWithCaching featureParser;
    private final Predicate<String> packageFilter;
    private final DiscoveryIssueReporter issueReporter;
    private final CucumberTestDescriptor.Builder testDescriptorBuilder;

    FeatureFileResolver(
            CucumberConfiguration configuration, Predicate<String> packageFilter, DiscoveryIssueReporter issueReporter
    ) {
        this.packageFilter = packageFilter;
        this.issueReporter = issueReporter;
        this.featureParser = createFeatureParser(configuration, issueReporter);
        this.featureScanner = new ResourceScanner<>(
            ClassLoaders::getDefaultClassLoader,
            FeatureIdentifier::isFeature,
            featureParser::parseResource);
        this.testDescriptorBuilder = CucumberTestDescriptor.builder(configuration);
    }

    private static FeatureParserWithCaching createFeatureParser(
            CucumberConfiguration options, DiscoveryIssueReporter issueReporter
    ) {
        Supplier<ClassLoader> classLoader = FeatureFileResolver.class::getClassLoader;
        UuidGeneratorServiceLoader uuidGeneratorServiceLoader = new UuidGeneratorServiceLoader(classLoader, options);
        UuidGenerator uuidGenerator = uuidGeneratorServiceLoader.loadUuidGenerator();
        FeatureParser featureParser = new FeatureParser(uuidGenerator::generateId);
        FeatureParserWithSource featureParserWithSource = new FeatureParserWithSource(featureParser);
        FeatureParserWithIssueReporting featureParserWithIssueReporting = new FeatureParserWithIssueReporting(
            featureParserWithSource, issueReporter);
        return new FeatureParserWithCaching(featureParserWithIssueReporting);
    }

    @Override
    public Resolution resolve(DiscoverySelector selector, Context context) {
        if (selector instanceof FeatureElementSelector elementSelector) {
            return resolve(elementSelector, context);
        }
        if (selector instanceof FeatureWithLinesSelector featureWithLinesSelector) {
            return resolve(featureWithLinesSelector);
        }
        return SelectorResolver.super.resolve(selector, context);
    }

    public Resolution resolve(FeatureElementSelector selector, Context context) {
        FeatureWithSource feature = selector.getFeature();
        Node selected = selector.getElement();
        return selected.getParent()
                .map(parent -> context.addToParent(() -> selectElement(feature, parent),
                    createTestDescriptor(feature, selected)))
                .orElseGet(() -> context.addToParent(createTestDescriptor(feature, selected)))
                .map(descriptor -> Match.exact(descriptor, () -> selectElementsOf(feature, selected)))
                .map(Resolution::match)
                .orElseGet(Resolution::unresolved);
    }

    private Function<TestDescriptor, Optional<TestDescriptor>> createTestDescriptor(
            FeatureWithSource feature, Node selected
    ) {
        return parent -> testDescriptorBuilder.build(parent, feature, selected);
    }

    public Resolution resolve(FeatureWithLinesSelector selector) {
        URI uri = selector.getUri();
        Set<DiscoverySelector> selectors = featureScanner
                .scanForResourcesUri(uri)
                .stream()
                .flatMap(feature -> selectElementsAt(feature, selector::getFilePositions, issueReporter))
                .collect(toSet());

        return toResolution(selectors);
    }

    @Override
    public Resolution resolve(FileSelector selector, Context context) {
        Path path = selector.getPath();
        if (!isFeature(path)) {
            return Resolution.unresolved();
        }

        Set<FeatureElementSelector> selectors = featureParser.parseResource(path)
                .map(feature -> selectElementAt(feature, selector::getPosition, issueReporter))
                .map(Collections::singleton)
                .orElseGet(Collections::emptySet);

        return toResolution(selectors);
    }

    @SuppressWarnings("deprecation") // TODO: Updagrade
    @Override
    public Resolution resolve(ClasspathResourceSelector selector, Context context) {
        Set<Resource> resources = selector.getClasspathResources();
        if (!resources.stream().allMatch(resource -> isFeature(resource.getName()))) {
            return resolveClasspathResourceSelectorAsPackageSelector(selector);
        }
        if (resources.size() > 1) {
            throw new IllegalArgumentException(String.format(
                "Found %s resources named %s on the classpath %s.",
                resources.size(), selector.getClasspathResourceName(),
                resources.stream().map(Resource::getUri).collect(toList())));
        }
        return resources.stream()
                .findFirst()
                .filter(resource -> isFeature(resource.getName()))
                .flatMap(featureParser::parseResource)
                .map(feature -> selectElementAt(feature, selector::getPosition, issueReporter))
                .map(Collections::singleton)
                .map(FeatureFileResolver::toResolution)
                .orElseGet(Resolution::unresolved);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    private Resolution resolveClasspathResourceSelectorAsPackageSelector(ClasspathResourceSelector selector) {
        Set<DiscoverySelector> selectors = featureScanner
                .scanForClasspathResource(selector.getClasspathResourceName(), packageFilter)
                .stream()
                .map(feature -> selectElementAt(feature, selector::getPosition, issueReporter))
                .collect(toSet());

        warnClasspathResourceSelectorUsedForPackage(selector);

        return toResolution(selectors);
    }

    private void warnClasspathResourceSelectorUsedForPackage(ClasspathResourceSelector selector) {
        String classpathResourceName = selector.getClasspathResourceName();
        String packageName = classpathResourceName.replaceAll("/", ".");
        String message = String.format(
            "The classpath resource selector '%s' should not be used to select features in a package. Use the package selector with '%s' instead",
            classpathResourceName,
            packageName);
        issueReporter.reportIssue(DiscoveryIssue.builder(WARNING, message));
    }

    @Override
    public Resolution resolve(UriSelector selector, Context context) {
        URI uri = selector.getUri();
        Set<DiscoverySelector> selectors = singleton(FeatureWithLinesSelector.from(uri));
        return toResolution(selectors);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        Class<?> javaClass = selector.getJavaClass();
        Cucumber annotation = javaClass.getAnnotation(Cucumber.class);
        if (annotation != null) {
            warnAboutDeprecatedCucumberClass(javaClass);
            String packageName = javaClass.getPackage().getName();
            Set<DiscoverySelector> selectors = singleton(selectPackage(packageName));
            return toResolution(selectors);
        }
        return Resolution.unresolved();
    }

    private void warnAboutDeprecatedCucumberClass(Class<?> javaClass) {
        String message = "The @Cucumber annotation has been deprecated. See the Javadoc for more details.";
        DiscoveryIssue issue = DiscoveryIssue.builder(WARNING, message)
                .source(ClassSource.from(javaClass))
                .build();
        issueReporter.reportIssue(issue);
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        UniqueId uniqueId = selector.getUniqueId();
        Set<FeatureWithLinesSelector> selectors = FeatureWithLinesSelector.from(uniqueId);
        return toResolution(selectors);
    }

    private static Resolution toResolution(Set<? extends DiscoverySelector> selectors) {
        if (selectors.isEmpty()) {
            return Resolution.unresolved();
        }
        return Resolution.selectors(selectors);
    }
}
