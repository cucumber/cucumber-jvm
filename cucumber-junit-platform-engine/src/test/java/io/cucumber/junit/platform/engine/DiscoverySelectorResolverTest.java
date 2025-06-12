package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.junit.platform.engine.CucumberTestDescriptor.FeatureDescriptor;
import io.cucumber.junit.platform.engine.CucumberTestDescriptor.PickleDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;

@WithLogRecordListener
class DiscoverySelectorResolverTest {

    private final DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();
    private CucumberEngineDescriptor testDescriptor;

    @BeforeEach
    void before() {
        UniqueId id = UniqueId.forEngine(new CucumberTestEngine().getId());
        testDescriptor = new CucumberEngineDescriptor(id,
            new CucumberConfiguration(new EmptyConfigurationParameters()));
        assertEquals(0, testDescriptor.getChildren().size());
    }

    private void resetTestDescriptor() {
        Set<? extends TestDescriptor> descendants = new HashSet<>(testDescriptor.getDescendants());
        descendants.forEach(o -> testDescriptor.removeChild(o));
    }

    @Test
    void resolveRequestWithMultipleUniqueIdSelector() {
        Set<UniqueId> selectors = new HashSet<>();

        DiscoverySelector resource = selectDirectory(
            "src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature");
        selectSomePickle(resource).ifPresent(selectors::add);

        DiscoverySelector resource2 = selectDirectory(
            "src/test/resources/io/cucumber/junit/platform/engine/single.feature");
        selectSomePickle(resource2).ifPresent(selectors::add);

        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(
            selectors.stream()
                    .map(DiscoverySelectors::selectUniqueId)
                    .collect(Collectors.toList()));

        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        assertEquals(
            selectors,
            testDescriptor.getDescendants()
                    .stream()
                    .filter(PickleDescriptor.class::isInstance)
                    .map(TestDescriptor::getUniqueId)
                    .collect(toSet()));
    }

    @Test
    void resolveRequestWithMultipleUniqueIdSelectorFromTheSameFeature() {
        Set<UniqueId> selectors = new HashSet<>();

        DiscoverySelector resource = selectDirectory(
            "src/test/resources/io/cucumber/junit/platform/engine/scenario-outline.feature");
        selectAllPickles(resource).forEach(selectors::add);

        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(
            selectors.stream()
                    .map(DiscoverySelectors::selectUniqueId)
                    .collect(Collectors.toList()));

        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        Set<String> pickleIdsFromFeature = testDescriptor.getDescendants()
                .stream()
                .filter(FeatureDescriptor.class::isInstance)
                .map(FeatureDescriptor.class::cast)
                .map(FeatureDescriptor::getFeature)
                .map(Feature::getPickles)
                .flatMap(Collection::stream)
                .map(Pickle::getId)
                .collect(toSet());

        Set<String> pickleIdsFromPickles = testDescriptor.getDescendants()
                .stream()
                .filter(PickleDescriptor.class::isInstance)
                .map(PickleDescriptor.class::cast)
                .map(PickleDescriptor::getPickle)
                .map(Pickle::getId)
                .collect(toSet());

        assertEquals(pickleIdsFromFeature, pickleIdsFromPickles);
    }

    private Optional<UniqueId> selectSomePickle(DiscoverySelector resource) {
        return selectAllPickles(resource).findFirst();
    }

    private Stream<UniqueId> selectAllPickles(DiscoverySelector resource) {
        EngineDiscoveryRequest discoveryRequest = new SelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        Set<? extends TestDescriptor> descendants = testDescriptor.getDescendants();
        resetTestDescriptor();
        return descendants.stream()
                .filter(PickleDescriptor.class::isInstance)
                .map(TestDescriptor::getUniqueId);
    }

}
