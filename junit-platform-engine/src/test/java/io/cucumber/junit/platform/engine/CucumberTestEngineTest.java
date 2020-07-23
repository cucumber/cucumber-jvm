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
import org.junit.platform.testkit.engine.Event;

import java.util.Optional;
import java.util.stream.Stream;

import static io.cucumber.junit.platform.engine.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
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
        assertEquals(2, result.testEvents().count()); // test start and finished
        assertEquals(1, result.testEvents().succeeded().count());
    }

    @Test
    void selectAndSkipDisabledScenarioByTags() {
        EngineExecutionResults result = EngineTestKit.engine("cucumber")
                .configurationParameter(FILTER_TAGS_PROPERTY_NAME, "@Integration and not @Disabled")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute();
        assertEquals(1, result.testEvents().count());
        assertEquals(1, result.testEvents().skipped().count());
        assertEquals(
            Optional.of(
                "'cucumber.filter.tags=( @Integration and not ( @Disabled ) )' did not match this scenario"),
            result.testEvents()
                    .skipped()
                    .map(Event::getPayload)
                    .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                    .findFirst());
    }

    @Test
    void selectAndSkipDisabledScenarioByName() {
        EngineExecutionResults result = EngineTestKit.engine("cucumber")
                .configurationParameter(FILTER_NAME_PROPERTY_NAME, "^Nothing$")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute();
        assertEquals(1, result.testEvents().count());
        assertEquals(1, result.testEvents().skipped().count());
        assertEquals(
            Optional.of("'cucumber.filter.name=^Nothing$' did not match this scenario"),
            result.testEvents()
                    .skipped()
                    .map(Event::getPayload)
                    .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                    .findFirst());
    }

}
