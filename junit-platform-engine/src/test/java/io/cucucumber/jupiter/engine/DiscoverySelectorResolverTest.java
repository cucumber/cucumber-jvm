package io.cucucumber.jupiter.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;

class DiscoverySelectorResolverTest {

    private final DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();
    private TestDescriptor testDescriptor;

    @BeforeEach
    void before() {
        CucumberTestEngine engine = new CucumberTestEngine();
        ConfigurationParameters configuration = new EmptyConfigurationParameters();
        EngineDiscoveryRequest discoveryRequest = new EmptyEngineDiscoveryRequest(configuration);
        UniqueId id = UniqueId.forEngine(engine.getId());
        testDescriptor = engine.discover(discoveryRequest, id);
        assertEquals(0, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithClasspathResourceSelector() {
        DiscoverySelector resource = selectClasspathResource("io/cucumber/jupiter/engine/single.feature");
        EngineDiscoveryRequest discoveryRequest = new SingleSelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(1, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithClasspathRootSelector() {
        DiscoverySelector resource = selectClasspathRoots(Collections.singleton(new File("src/test/resources/").toPath())).get(0);
        EngineDiscoveryRequest discoveryRequest = new SingleSelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(2, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithFileSelector() {
        DiscoverySelector resource = selectFile("src/test/resources/io/cucumber/jupiter/engine/single.feature");
        EngineDiscoveryRequest discoveryRequest = new SingleSelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(1, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithDirectorySelector() {
        DiscoverySelector resource = selectDirectory("src/test/resources/io/cucumber/jupiter/engine");
        EngineDiscoveryRequest discoveryRequest = new SingleSelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(2, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithPackageSelector() {
        DiscoverySelector resource = selectPackage("io.cucumber.jupiter.engine");
        EngineDiscoveryRequest discoveryRequest = new SingleSelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);
        assertEquals(2, testDescriptor.getChildren().size());
    }

    @Test
    void resolveRequestWithUniqueIdSelector() {
        DiscoverySelector resource = selectPackage("io.cucumber.jupiter.engine");
        EngineDiscoveryRequest discoveryRequest = new SingleSelectorRequest(resource);
        resolver.resolveSelectors(discoveryRequest, testDescriptor);

        Set<? extends TestDescriptor> descendants = new HashSet<>(testDescriptor.getDescendants());
        resetTestDescriptor();

        descendants.forEach(targetDescriptor -> resolveRequestWithUniqueIdSelector(targetDescriptor.getUniqueId()));
    }

    private void resolveRequestWithUniqueIdSelector(UniqueId targetId) {
        resetTestDescriptor();

        UniqueIdSelector uniqueIdSelector = selectUniqueId(targetId);
        EngineDiscoveryRequest descendantRequest = new SingleSelectorRequest(uniqueIdSelector);
        resolver.resolveSelectors(descendantRequest, testDescriptor);
        testDescriptor.getDescendants()
            .stream()
            .filter(TestDescriptor::isTest)
            .map(TestDescriptor::getUniqueId)
            .forEach(selectedId -> assertTrue(selectedId.hasPrefix(targetId), selectedId + " has prefix " + targetId));
    }

    private void resetTestDescriptor() {
        Set<? extends TestDescriptor> descendants = new HashSet<>(testDescriptor.getDescendants());
        descendants.forEach(o -> testDescriptor.removeChild(o));
    }

    private static class SingleSelectorRequest implements EngineDiscoveryRequest {

        private final DiscoverySelector resource;

        SingleSelectorRequest(DiscoverySelector resource) {
            this.resource = resource;
        }

        @Override
        public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            if (selectorType.isInstance(resource)) {
                return Collections.singletonList((T) resource);
            }

            return Collections.emptyList();
        }

        @Override
        public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> filterType) {
            return Collections.emptyList();
        }

        @Override
        public ConfigurationParameters getConfigurationParameters() {
            return new EmptyConfigurationParameters();
        }
    }
}