package io.cucumber.junit.platform.engine;

import io.cucumber.junit.platform.engine.CucumberDiscoverySelectors.FeatureWithLinesSelector;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

import java.util.List;
import java.util.Set;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.engine.DiscoveryIssue.Severity.WARNING;

/**
 * Decorator to support resolving the
 * {@value io.cucumber.junit.platform.engine.Constants#FEATURES_PROPERTY_NAME}
 * property.
 * <p>
 * The JUnit Platform provides various discovery selectors to select feature
 * files. Unfortunately, these do not yet receive support from IDEs, Maven or
 * Gradle. Resolving this property allows uses to target a single feature,
 * scenario or example from the commandline.
 * <p>
 * This class decorates the {@link DiscoverySelectorResolver}. When the features
 * property is provided it replaces the discovery request.
 * <p>
 * Note: This effectively causes Cucumber to ignore any requests from the JUnit
 * Platform. So features will be discovered even when none are expected to be.
 */
class FeaturesPropertyResolver {

    private final DiscoverySelectorResolver delegate;

    FeaturesPropertyResolver(DiscoverySelectorResolver delegate) {
        this.delegate = delegate;
    }

    void resolveSelectors(
            EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor,
            DiscoveryIssueReporter issueReporter
    ) {
        ConfigurationParameters configuration = request.getConfigurationParameters();
        CucumberConfiguration options = new CucumberConfiguration(configuration);
        Set<FeatureWithLinesSelector> selectors = options.featuresWithLines();

        if (selectors.isEmpty()) {
            delegate.resolveSelectors(request, engineDescriptor, issueReporter);
            return;
        }
        issueReporter.reportIssue(createCucumberFeaturesPropertyIsUsedIssue());
        EngineDiscoveryRequest replacement = new FeaturesPropertyDiscoveryRequest(request, selectors);
        delegate.resolveSelectors(replacement, engineDescriptor, issueReporter);
    }

    private static DiscoveryIssue createCucumberFeaturesPropertyIsUsedIssue() {
        return DiscoveryIssue.create(WARNING,
            "Discovering tests using the " + FEATURES_PROPERTY_NAME + " property. Other discovery " +
                    "selectors are ignored!\n" +
                    "\n" +
                    "This is a work around for the limited JUnit 5 support in Maven and Gradle. " +
                    "Please request/upvote/sponsor/ect better support for JUnit 5 discovery selectors. " +
                    "For details see: https://github.com/cucumber/cucumber-jvm/pull/2498\n" +
                    "\n" +
                    "If you are using the JUnit 5 Suite Engine, Platform Launcher API or Console Launcher you " +
                    "should not use this property. Please consult the JUnit 5 documentation on test selection.");
    }

    private static class FeaturesPropertyDiscoveryRequest implements EngineDiscoveryRequest {

        private final EngineDiscoveryRequest delegate;
        private final Set<? extends DiscoverySelector> selectors;

        public FeaturesPropertyDiscoveryRequest(
                EngineDiscoveryRequest delegate,
                Set<? extends DiscoverySelector> selectors
        ) {
            this.delegate = delegate;
            this.selectors = selectors;
        }

        @Override
        public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            requireNonNull(selectorType);
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
