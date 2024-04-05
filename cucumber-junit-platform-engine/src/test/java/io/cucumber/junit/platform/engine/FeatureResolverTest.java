package io.cucumber.junit.platform.engine;

import io.cucumber.junit.platform.engine.NodeDescriptor.PickleDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_EXCLUSIVE_RESOURCES_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_MODE_FEATURE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.TestDescriptor.Type.CONTAINER;
import static org.junit.platform.engine.TestDescriptor.Type.TEST;
import static org.junit.platform.engine.TestTag.create;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.support.descriptor.ClasspathResourceSource.from;
import static org.junit.platform.engine.support.descriptor.FilePosition.from;

class FeatureResolverTest {

    private final String featurePath = "io/cucumber/junit/platform/engine/feature-with-outline.feature";
    private final String featureSegmentValue = CLASSPATH_SCHEME_PREFIX + featurePath;
    private final UniqueId id = UniqueId.forEngine(new CucumberTestEngine().getId());
    private final CucumberEngineDescriptor engineDescriptor = new CucumberEngineDescriptor(id);
    private ConfigurationParameters configurationParameters = new EmptyConfigurationParameters();

    @Test
    void feature() {
        TestDescriptor feature = getFeature();
        assertEquals("A feature with scenario outlines", feature.getDisplayName());
        assertEquals(emptySet(), feature.getTags());
        assertEquals(of(from(featurePath)), feature.getSource());
        assertEquals(CONTAINER, feature.getType());
        assertEquals(
            id.append("feature", featureSegmentValue),
            feature.getUniqueId());
    }

    private TestDescriptor getFeature() {
        FeatureResolver featureResolver = FeatureResolver.create(configurationParameters, engineDescriptor,
            aPackage -> true);
        featureResolver.resolveClasspathResource(selectClasspathResource(featurePath));
        Set<? extends TestDescriptor> features = engineDescriptor.getChildren();
        return features.iterator().next();
    }

    @Test
    void scenario() {
        TestDescriptor scenario = getScenario();
        assertEquals("A scenario", scenario.getDisplayName());
        assertEquals(
            asSet(create("FeatureTag"), create("ScenarioTag"), create("ResourceA"), create("ResourceAReadOnly")),
            scenario.getTags());
        assertEquals(of(from(featurePath, from(5, 3))), scenario.getSource());
        assertEquals(TEST, scenario.getType());
        assertEquals(
            id.append("feature", featureSegmentValue)
                    .append("scenario", "5"),
            scenario.getUniqueId());
        PickleDescriptor pickleDescriptor = (PickleDescriptor) scenario;
        assertEquals(Optional.of("io.cucumber.junit.platform.engine"), pickleDescriptor.getPackage());
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
    void outline() {
        TestDescriptor outline = getOutline();
        assertEquals("A scenario outline", outline.getDisplayName());
        assertEquals(
            emptySet(),
            outline.getTags());
        assertEquals(of(from(featurePath, from(11, 3))), outline.getSource());
        assertEquals(CONTAINER, outline.getType());
        assertEquals(
            id.append("feature", featureSegmentValue)
                    .append("scenario", "11"),
            outline.getUniqueId());
    }

    private TestDescriptor getOutline() {
        Iterator<? extends TestDescriptor> iterator = getFeature().getChildren().iterator();
        iterator.next();
        return iterator.next();
    }

    @Test
    void example() {
        TestDescriptor example = getExample();
        assertEquals("Example #1.1", example.getDisplayName());
        assertEquals(
            asSet(create("FeatureTag"), create("Example1Tag"), create("ScenarioOutlineTag")),
            example.getTags());
        assertEquals(of(from(featurePath, from(19, 7))), example.getSource());
        assertEquals(TEST, example.getType());

        assertEquals(
            id.append("feature", featureSegmentValue)
                    .append("scenario", "11")
                    .append("examples", "17")
                    .append("example", "19"),
            example.getUniqueId());

        PickleDescriptor pickleDescriptor = (PickleDescriptor) example;
        assertEquals(Optional.of("io.cucumber.junit.platform.engine"), pickleDescriptor.getPackage());
    }

    @Test
    void longNames() {
        configurationParameters = new MapConfigurationParameters(
            JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long");

        TestDescriptor example = getExample();
        assertEquals("A feature with scenario outlines - A scenario outline - With some text - Example #1.1",
            example.getDisplayName());
    }

    @Test
    void longNamesWithPickleNames() {
        configurationParameters = new MapConfigurationParameters(Map.of(
            JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "long",
            JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME, "pickle"));

        TestDescriptor example = getExample();
        assertEquals("A feature with scenario outlines - A scenario outline - With some text - A scenario outline",
            example.getDisplayName());
    }

    @Test
    void shortNamesWithExampleNumbers() {
        configurationParameters = new MapConfigurationParameters(
            JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME, "number");

        TestDescriptor example = getExample();
        assertEquals("Example #1.1", example.getDisplayName());
    }

    @Test
    void shortNamesWithPickleNames() {
        configurationParameters = new MapConfigurationParameters(Map.of(
            JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, "short",
            JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME, "pickle"));

        TestDescriptor example = getExample();
        assertEquals("A scenario outline", example.getDisplayName());
    }

    private TestDescriptor getExample() {
        return getOutline().getChildren().iterator().next().getChildren().iterator().next();
    }

    @Test
    void parallelExecutionForFeaturesEnabled() {
        configurationParameters = new MapConfigurationParameters(
            EXECUTION_MODE_FEATURE_PROPERTY_NAME, "concurrent");

        assertTrue(getNodes().size() > 0);
        assertTrue(getPickles().size() > 0);
        getNodes().forEach(node -> assertEquals(Node.ExecutionMode.CONCURRENT, node.getExecutionMode()));
        getPickles().forEach(pickle -> assertEquals(Node.ExecutionMode.CONCURRENT, pickle.getExecutionMode()));
    }

    @Test
    void parallelExecutionForFeaturesDisabled() {
        configurationParameters = new MapConfigurationParameters(
            EXECUTION_MODE_FEATURE_PROPERTY_NAME, "same_thread");

        assertTrue(getNodes().size() > 0);
        assertTrue(getPickles().size() > 0);
        getNodes().forEach(node -> assertEquals(Node.ExecutionMode.SAME_THREAD, node.getExecutionMode()));
        getPickles().forEach(pickle -> assertEquals(Node.ExecutionMode.SAME_THREAD, pickle.getExecutionMode()));
    }

    private Set<NodeDescriptor> getNodes() {
        return getFeature().getChildren().stream()
                .filter(TestDescriptor::isContainer)
                .map(node -> (NodeDescriptor) node)
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
