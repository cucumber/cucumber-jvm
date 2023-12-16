package io.cucumber.junit.platform.engine;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.engine.Filter.composeFilters;

class DiscoverySelectorResolver {

    private static final Logger log = LoggerFactory.getLogger(DiscoverySelectorResolver.class);

    // @formatter:off
    private static final EngineDiscoveryRequestResolver<CucumberEngineDescriptor> resolver = EngineDiscoveryRequestResolver
            .<CucumberEngineDescriptor>builder()
            .addSelectorResolver(context -> new FeatureResolver(
                    context.getDiscoveryRequest().getConfigurationParameters(),
                    getPackageFilter(context.getDiscoveryRequest()))
            )
            .addTestDescriptorVisitor(context -> new PackageFilteringVisitor(getPackageFilter(context.getDiscoveryRequest())))
            .addTestDescriptorVisitor(context -> new FeatureOrderingVisitor())
            .addTestDescriptorVisitor(context -> new FeatureElementOrderingVisitor())
            .addTestDescriptorVisitor(context -> TestDescriptor::prune)
            .build();
    // @formatter:on

    private static boolean warnedWhenCucumberFeaturesPropertyIsUsed = false;

    private static void warnWhenCucumberFeaturesPropertyIsUsed() {
        if (warnedWhenCucumberFeaturesPropertyIsUsed) {
            return;
        }
        warnedWhenCucumberFeaturesPropertyIsUsed = true;
        log.warn(
            () -> "Discovering tests using the " + FEATURES_PROPERTY_NAME
                    + " property. Other discovery selectors are ignored!\n" +
                    "Please request/upvote/sponsor/ect better support for JUnit 5 discovery selectors.\n" +
                    "See: https://github.com/cucumber/cucumber-jvm/pull/2498");
    }

    void resolveSelectors(EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor) {
        ConfigurationParameters configuration = request.getConfigurationParameters();
        CucumberEngineOptions options = new CucumberEngineOptions(configuration);
        Set<CucumberDiscoverySelectors.FeatureWithLinesSelector> featureWithLines = options.featuresWithLines();
        if (!featureWithLines.isEmpty()) {
            warnWhenCucumberFeaturesPropertyIsUsed();
            request = new CucumberFeaturesPropertyDiscoveryRequest(request, featureWithLines);
        }
        resolver.resolve(request, engineDescriptor);
    }

    private static Predicate<String> getPackageFilter(EngineDiscoveryRequest request) {
        // TODO: Move into JUnit.
        return composeFilters(request.getFiltersByType(PackageNameFilter.class)).toPredicate();
    }

    private static class CucumberFeaturesPropertyDiscoveryRequest implements EngineDiscoveryRequest {

        private final EngineDiscoveryRequest delegate;
        private final Set<? extends DiscoverySelector> selectors;

        public CucumberFeaturesPropertyDiscoveryRequest(
                EngineDiscoveryRequest delegate,
                Set<? extends DiscoverySelector> selectors
        ) {
            this.delegate = delegate;
            this.selectors = selectors;
        }

        @Override
        public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            Objects.requireNonNull(selectorType);
            return this.selectors.stream().filter(selectorType::isInstance).map(selectorType::cast).collect(toList());
        }

        @Override
        public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> filterType) {
            return delegate.getFiltersByType(filterType);
        }

        @Override
        public ConfigurationParameters getConfigurationParameters() {
            return delegate.getConfigurationParameters();
        }
    }

}
