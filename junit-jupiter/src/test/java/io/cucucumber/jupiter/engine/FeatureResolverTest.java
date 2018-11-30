package io.cucucumber.jupiter.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import static io.cucucumber.jupiter.engine.FeatureResolver.createFeatureResolver;
import static java.util.Collections.emptySet;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.support.descriptor.FilePosition.from;
import static org.junit.platform.engine.support.descriptor.FileSource.from;

class FeatureResolverTest {
    private final String featurePath = "io/cucumber/jupiter/engine/feature-with-outline.feature";
    private final File featureFile = new File(featurePath);

    private TestDescriptor testDescriptor;
    private UniqueId id;


    @BeforeEach
    void before() {
        CucumberTestEngine engine = new CucumberTestEngine();
        ConfigurationParameters configuration = new EmptyConfigurationParameters();
        EngineDiscoveryRequest discoveryRequest = new EmptyEngineDiscoveryRequest(configuration);
        id = UniqueId.forEngine(engine.getId());
        testDescriptor = engine.discover(discoveryRequest, id);
        FeatureResolver featureResolver = createFeatureResolver(testDescriptor);
        featureResolver.resolveClassPathResource(featurePath);
    }

    @Test
    void feature() {
        TestDescriptor feature = getFeature();
        assertEquals("A feature with scenario outlines", feature.getDisplayName());
        assertEquals(emptySet(), feature.getTags());
        assertEquals(of(from(featureFile)), feature.getSource());
        assertEquals(TestDescriptor.Type.CONTAINER, feature.getType());
        assertEquals(
            id.append("feature", featurePath),
            feature.getUniqueId()
        );
    }

    @Test
    void scenario() {
        TestDescriptor scenario = getScenario();
        assertEquals("A scenario", scenario.getDisplayName());
        assertEquals(emptySet(), scenario.getTags());
        assertEquals(of(from(featureFile, from(3, 3))), scenario.getSource());
        assertEquals(TestDescriptor.Type.TEST, scenario.getType());
        assertEquals(
            id.append("feature", featurePath).append("scenario", "3"),
            scenario.getUniqueId()
        );
    }

    @Test
    void outline() {
        TestDescriptor outline = getOutline();
        assertEquals("A scenario outline", outline.getDisplayName());
        assertEquals(emptySet(), outline.getTags());
        assertEquals(of(from(featureFile, from(8, 3))), outline.getSource());
        assertEquals(TestDescriptor.Type.CONTAINER, outline.getType());
        assertEquals(id.append(
            "feature", featurePath).append("outline", "8"),
            outline.getUniqueId()
        );
    }

    @Test
    void example() {
        TestDescriptor example = getExample();
        assertEquals("Example #1", example.getDisplayName());
        assertEquals(emptySet(), example.getTags());
        assertEquals(of(from(featureFile, from(15, 8))), example.getSource());
        assertEquals(TestDescriptor.Type.TEST, example.getType());

        assertEquals(
            id.append("feature", featurePath).append("outline", "8").append("example", "15"),
            example.getUniqueId()
        );
    }

    private TestDescriptor getFeature() {
        Set<? extends TestDescriptor> features = testDescriptor.getChildren();
        return features.iterator().next();
    }

    private TestDescriptor getScenario() {
        return getFeature().getChildren().iterator().next();
    }

    private TestDescriptor getOutline() {
        Iterator<? extends TestDescriptor> iterator = getFeature().getChildren().iterator();
        iterator.next();
        return iterator.next();
    }

    private TestDescriptor getExample() {
        return getOutline().getChildren().iterator().next();
    }
}