package io.cucucumber.jupiter.engine;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CucumberTestEngineTest {

    private final CucumberTestEngine engine = new CucumberTestEngine();

    @Test
    void id() {
        assertEquals("cucumber", engine.getId());
    }

    @Test
    void groupId() {
        assertEquals("io.cucucumber", engine.getGroupId().get());
    }

    @Test
    void artifactId() {
        assertEquals("cucucumber-junit-jupiter", engine.getArtifactId().get());
    }

    @Test
    void version() {
        assertEquals("DEVELOPMENT", engine.getVersion().get());
    }


    @Test
    void createExecutionContext(){
        EngineExecutionListener listener = new EmptyEngineExecutionListener();
        ConfigurationParameters config = new EmptyConfigurationParameters();
        EngineDiscoveryRequest discovery = new EmptyEngineDiscoveryRequest(config);
        UniqueId id = UniqueId.forEngine(engine.getId());
        TestDescriptor discover = engine.discover(discovery, id);
        ExecutionRequest execution = new ExecutionRequest(discover, listener, config);
        assertNotNull(engine.createExecutionContext(execution));
    }

    private static class EmptyConfigurationParameters implements ConfigurationParameters {
        @Override
        public Optional<String> get(String key) {
            return Optional.empty();
        }

        @Override
        public Optional<Boolean> getBoolean(String key) {
            return Optional.empty();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static class EmptyEngineExecutionListener implements EngineExecutionListener {
        @Override
        public void dynamicTestRegistered(TestDescriptor testDescriptor) {

        }

        @Override
        public void executionSkipped(TestDescriptor testDescriptor, String reason) {

        }

        @Override
        public void executionStarted(TestDescriptor testDescriptor) {

        }

        @Override
        public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {

        }

        @Override
        public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {

        }
    }

    private static class EmptyEngineDiscoveryRequest implements EngineDiscoveryRequest {
        private final ConfigurationParameters config;

        EmptyEngineDiscoveryRequest(ConfigurationParameters config) {
            this.config = config;
        }

        @Override
        public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            return Collections.emptyList();
        }

        @Override
        public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> filterType) {
            return Collections.emptyList();
        }

        @Override
        public ConfigurationParameters getConfigurationParameters() {
            return config;
        }
    }
}
