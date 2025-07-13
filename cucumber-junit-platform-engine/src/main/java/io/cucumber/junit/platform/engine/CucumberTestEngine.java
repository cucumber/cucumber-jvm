package io.cucumber.junit.platform.engine;

import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_DISCOVERY_AS_ROOT_ENGINE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_CONFIG_PREFIX;
import static org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.deduplicating;
import static org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.forwarding;

/**
 * The Cucumber {@link org.junit.platform.engine.TestEngine TestEngine}.
 * <p>
 * Supports discovery and execution of {@code .feature} files using the
 * following selectors:
 * <ul>
 * <li>{@link org.junit.platform.engine.discovery.ClasspathRootSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.ClasspathResourceSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.PackageSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.FileSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.DirectorySelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.UniqueIdSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.UriSelector}</li>
 * </ul>
 */
@API(status = API.Status.STABLE)
public final class CucumberTestEngine extends HierarchicalTestEngine<CucumberEngineExecutionContext> {

    @Override
    public String getId() {
        return "cucumber";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
        TestSource testSource = createEngineTestSource(configurationParameters);
        CucumberConfiguration configuration = new CucumberConfiguration(configurationParameters);
        CucumberEngineDescriptor engineDescriptor = new CucumberEngineDescriptor(uniqueId, configuration, testSource);

        DiscoveryIssueReporter issueReporter = deduplicating(forwarding( //
            discoveryRequest.getDiscoveryListener(), //
            engineDescriptor.getUniqueId() //
        ));

        // Early out if Cucumber is the root engine and discovery has been
        // explicitly disabled. Workaround for:
        // https://github.com/sbt/sbt-jupiter-interface/issues/142
        if (!supportsDiscoveryAsRootEngine(configurationParameters) && isRootEngine(uniqueId)) {
            return engineDescriptor;
        }

        FeaturesPropertyResolver resolver = new FeaturesPropertyResolver(new DiscoverySelectorResolver());
        resolver.resolveSelectors(discoveryRequest, engineDescriptor, issueReporter);
        return engineDescriptor;
    }

    private static boolean supportsDiscoveryAsRootEngine(ConfigurationParameters configurationParameters) {
        return configurationParameters.getBoolean(JUNIT_PLATFORM_DISCOVERY_AS_ROOT_ENGINE_PROPERTY_NAME)
                .orElse(true);
    }

    private boolean isRootEngine(UniqueId uniqueId) {
        UniqueId cucumberRootEngineId = UniqueId.forEngine(getId());
        return uniqueId.hasPrefix(cucumberRootEngineId);
    }

    private static TestSource createEngineTestSource(ConfigurationParameters configurationParameters) {
        // Workaround. Test Engines do not normally have test source.
        // Maven does not count tests that do not have a ClassSource somewhere
        // in the test descriptor tree.
        // Gradle will report all tests as coming from an "Unknown Class"
        // See: https://github.com/cucumber/cucumber-jvm/pull/2498
        if (configurationParameters.get(FEATURES_PROPERTY_NAME).isPresent()) {
            return ClassSource.from(CucumberTestEngine.class);
        }
        return null;
    }

    @Override
    protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
        CucumberConfiguration configuration = getCucumberConfiguration(request);
        if (configuration.isParallelExecutionEnabled()) {
            return new ForkJoinPoolHierarchicalTestExecutorService(
                new PrefixedConfigurationParameters(request.getConfigurationParameters(), PARALLEL_CONFIG_PREFIX));
        }
        return super.createExecutorService(request);
    }

    @Override
    protected CucumberEngineExecutionContext createExecutionContext(ExecutionRequest request) {
        CucumberConfiguration configuration = getCucumberConfiguration(request);
        return new CucumberEngineExecutionContext(configuration);
    }

    private CucumberConfiguration getCucumberConfiguration(ExecutionRequest request) {
        CucumberEngineDescriptor engineDescriptor = (CucumberEngineDescriptor) request.getRootTestDescriptor();
        return engineDescriptor.getConfiguration();
    }

}
