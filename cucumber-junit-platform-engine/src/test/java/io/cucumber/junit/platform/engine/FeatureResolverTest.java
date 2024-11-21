package io.cucumber.junit.platform.engine;

import io.cucumber.junit.platform.engine.FeatureElementDescriptor.PickleDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static io.cucumber.junit.platform.engine.Constants.EXECUTION_EXCLUSIVE_RESOURCES_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_MODE_FEATURE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;

// TODO: Move this into CucumberTestEngineTest
class FeatureResolverTest {

    private final String featurePath = "io/cucumber/junit/platform/engine/scenario-outline.feature";
    private final UniqueId id = UniqueId.forEngine(new CucumberTestEngine().getId());
    private ConfigurationParameters configurationParameters = new EmptyConfigurationParameters();
    private final CucumberEngineDescriptor engineDescriptor = new CucumberEngineDescriptor(id,
        new CucumberConfiguration(configurationParameters));

    private TestDescriptor getFeature() {
        EngineDiscoveryRequestResolver<CucumberEngineDescriptor> resolver = EngineDiscoveryRequestResolver
                .<CucumberEngineDescriptor> builder()
                .addSelectorResolver(context -> new FeatureResolver(new CucumberConfiguration(configurationParameters),
                    context.getPackageFilter()))
                .addTestDescriptorVisitor(context -> new FeatureElementOrderingVisitor())
                .build();
        resolver.resolve(new SelectorRequest(selectClasspathResource(featurePath)), engineDescriptor);
        Set<? extends TestDescriptor> features = engineDescriptor.getChildren();
        return features.iterator().next();
    }

    @Test
    void exclusiveResources() {
        configurationParameters = new MapConfigurationParameters(
            new HashMap<String, String>() {
                {
                    put(EXECUTION_EXCLUSIVE_RESOURCES_PREFIX + "ResourceA" + READ_WRITE_SUFFIX, "resource-a");
                    put(EXECUTION_EXCLUSIVE_RESOURCES_PREFIX + "ResourceAReadOnly" + READ_SUFFIX, "resource-a");
                }
            });

        PickleDescriptor pickleDescriptor = (PickleDescriptor) getScenario();
        assertEquals(
            asSet(
                new ExclusiveResource("resource-a", LockMode.READ_WRITE),
                new ExclusiveResource("resource-a", LockMode.READ)),
            pickleDescriptor.getExclusiveResources());
    }

    private TestDescriptor getScenario() {
        return getFeature().getChildren().iterator().next();
    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... tags) {
        return new HashSet<>(asList(tags));
    }

    @Test
    void parallelExecutionForFeaturesEnabled() {
        configurationParameters = new MapConfigurationParameters(
            EXECUTION_MODE_FEATURE_PROPERTY_NAME, "concurrent");

        assertFalse(getNodes().isEmpty());
        assertFalse(getPickles().isEmpty());
        getNodes().forEach(node -> assertEquals(Node.ExecutionMode.CONCURRENT, node.getExecutionMode()));
        getPickles().forEach(pickle -> assertEquals(Node.ExecutionMode.CONCURRENT, pickle.getExecutionMode()));
    }

    @Test
    void parallelExecutionForFeaturesDisabled() {
        configurationParameters = new MapConfigurationParameters(
            EXECUTION_MODE_FEATURE_PROPERTY_NAME, "same_thread");

        assertFalse(getNodes().isEmpty());
        assertFalse(getPickles().isEmpty());
        getNodes().forEach(node -> assertEquals(Node.ExecutionMode.SAME_THREAD, node.getExecutionMode()));
        getPickles().forEach(pickle -> assertEquals(Node.ExecutionMode.SAME_THREAD, pickle.getExecutionMode()));
    }

    private Set<FeatureElementDescriptor> getNodes() {
        return getFeature().getChildren().stream()
                .filter(TestDescriptor::isContainer)
                .map(node -> (FeatureElementDescriptor) node)
                .collect(Collectors.toSet());
    }

    private Set<PickleDescriptor> getPickles() {
        return getFeature().getChildren().stream()
                .filter(TestDescriptor::isContainer)
                .flatMap(examplesNode -> examplesNode.getChildren().stream())
                .flatMap(exampleNode -> exampleNode.getChildren().stream())
                .map(example -> (PickleDescriptor) example)
                .collect(Collectors.toSet());
    }
}
