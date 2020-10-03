package io.cucumber.junit.platform.engine;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.Optional;

import static io.cucumber.junit.platform.engine.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.CucumberEngineDescriptor.ENGINE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.skippedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.test;

class CucumberTestEngineTest {

    private final CucumberTestEngine engine = new CucumberTestEngine();

    @Test
    void id() {
        assertEquals(ENGINE_ID, engine.getId());
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
        EngineTestKit.engine(ENGINE_ID)
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(2, event(test()))
                .haveExactly(1, event(finishedSuccessfully()));
    }

    @Test
    void selectAndSkipDisabledScenarioByTags() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(FILTER_TAGS_PROPERTY_NAME, "@Integration and not @Disabled")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(1, event(test()))
                .haveExactly(1, event(skippedWithReason(
                    "'cucumber.filter.tags=( @Integration and not ( @Disabled ) )' did not match this scenario")));
    }

    @Test
    void selectAndSkipDisabledScenarioByName() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(FILTER_NAME_PROPERTY_NAME, "^Nothing$")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(1, event(test()))
                .haveExactly(1,
                    event(skippedWithReason("'cucumber.filter.name=^Nothing$' did not match this scenario")));
    }

}
