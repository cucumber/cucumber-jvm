package io.cucumber.junit.platform.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_EXCLUSIVE_RESOURCES_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static io.cucumber.junit.platform.engine.FeatureResolver.createFeatureResolver;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.TestDescriptor.Type.CONTAINER;
import static org.junit.platform.engine.TestDescriptor.Type.TEST;
import static org.junit.platform.engine.TestTag.create;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.support.descriptor.ClasspathResourceSource.from;
import static org.junit.platform.engine.support.descriptor.FilePosition.from;

class FeatureResolverTest {

    private final String featurePath = "io/cucumber/junit/platform/engine/feature-with-outline.feature";
    private final String featureSegmentValue = CLASSPATH_SCHEME_PREFIX + featurePath;
    private CucumberEngineDescriptor testDescriptor;
    private UniqueId id;

    @BeforeEach
    void before() {

        ConfigurationParameters configurationParameters = new MapConfigurationParameters(
            new HashMap<String, String>() {
                {
                    put(EXECUTION_EXCLUSIVE_RESOURCES_PREFIX + "ResourceA" + READ_WRITE_SUFFIX, "resource-a");
                    put(EXECUTION_EXCLUSIVE_RESOURCES_PREFIX + "ResourceAReadOnly" + READ_SUFFIX, "resource-a");
                }
            });
        EmptyEngineDiscoveryRequest request = new EmptyEngineDiscoveryRequest(configurationParameters);
        id = UniqueId.forEngine(new CucumberTestEngine().getId());
        testDescriptor = new CucumberEngineDescriptor(id);
        FeatureResolver featureResolver = createFeatureResolver(request.getConfigurationParameters(), testDescriptor,
            aPackage -> true);
        featureResolver.resolveClasspathResource(selectClasspathResource(featurePath));
    }

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
        Set<? extends TestDescriptor> features = testDescriptor.getChildren();
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
        assertEquals("Example #1", example.getDisplayName());
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

    private TestDescriptor getExample() {
        return getOutline().getChildren().iterator().next().getChildren().iterator().next();
    }

}
