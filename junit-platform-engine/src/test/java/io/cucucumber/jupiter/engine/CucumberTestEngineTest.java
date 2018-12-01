package io.cucucumber.jupiter.engine;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

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
        ConfigurationParameters configuration = new EmptyConfigurationParameters();
        EngineDiscoveryRequest discoveryRequest = new EmptyEngineDiscoveryRequest(configuration);
        UniqueId id = UniqueId.forEngine(engine.getId());
        TestDescriptor testDescriptor = engine.discover(discoveryRequest, id);
        ExecutionRequest execution = new ExecutionRequest(testDescriptor, listener, configuration);
        assertNotNull(engine.createExecutionContext(execution));
    }

}
