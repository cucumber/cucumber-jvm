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
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_CONFIG_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;

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
        TestSource testSource = createEngineTestSource(discoveryRequest);
        CucumberEngineDescriptor engineDescriptor = new CucumberEngineDescriptor(uniqueId, testSource);
        new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
        return engineDescriptor;
    }

    private static TestSource createEngineTestSource(EngineDiscoveryRequest discoveryRequest) {
        // Workaround. Test Engines do not normally have test source.
        // Maven does not count tests that do not have a ClassSource somewhere
        // in the test descriptor tree.
        // Gradle will report all tests as coming from an "Unknown Class"
        // See: https://github.com/cucumber/cucumber-jvm/pull/2498
        ConfigurationParameters configuration = discoveryRequest.getConfigurationParameters();
        if (configuration.get(FEATURES_PROPERTY_NAME).isPresent()) {
            return ClassSource.from(CucumberTestEngine.class);
        }
        return null;
    }

    @Override
    protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
        ConfigurationParameters config = request.getConfigurationParameters();
        if (config.getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME).orElse(false)) {
            return new ForkJoinPoolHierarchicalTestExecutorService(
                new PrefixedConfigurationParameters(config, PARALLEL_CONFIG_PREFIX));
        }
        return super.createExecutorService(request);
    }

    @Override
    protected CucumberEngineExecutionContext createExecutionContext(ExecutionRequest request) {
        return new CucumberEngineExecutionContext(request.getConfigurationParameters());
    }

}
