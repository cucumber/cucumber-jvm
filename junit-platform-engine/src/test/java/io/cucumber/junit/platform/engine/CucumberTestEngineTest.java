package io.cucumber.junit.platform.engine;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;

class CucumberTestEngineTest {

    private final CucumberTestEngine engine = new CucumberTestEngine();

    @Test
    void id() {
        assertEquals("cucumber", engine.getId());
    }

    @Test
    void version() {
        assertEquals(Optional.of("DEVELOPMENT"), engine.getVersion());
    }


    @Test
    void createExecutionContext() {
        EngineExecutionListener listener = new EmptyEngineExecutionListener();
        ConfigurationParameters configuration = new EmptyConfigurationParameters();
        EngineDiscoveryRequest discoveryRequest = new EmptyEngineDiscoveryRequest(configuration);
        UniqueId id = UniqueId.forEngine(engine.getId());
        TestDescriptor testDescriptor = engine.discover(discoveryRequest, id);
        ExecutionRequest execution = new ExecutionRequest(testDescriptor, listener, configuration);
        assertNotNull(engine.createExecutionContext(execution));
    }

    @Test
    void selectAndExecuteSingleScenario() {
        EngineExecutionResults result = EngineTestKit.engine("cucumber")
            .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
            .execute();
        assertEquals(0, result.tests().failed().count());
        assertEquals(1, result.tests().succeeded().count());
    }

}
