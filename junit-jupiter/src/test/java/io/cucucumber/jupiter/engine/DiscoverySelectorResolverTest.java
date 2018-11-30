package io.cucucumber.jupiter.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

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


    private static class SingleSelectorRequest implements EngineDiscoveryRequest {

        private final DiscoverySelector resource;

        public SingleSelectorRequest(DiscoverySelector resource) {
            this.resource = resource;
        }

        @Override
        public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            if (selectorType.isInstance(resource)) {
                return Collections.singletonList((T)resource);
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