package io.cucumber.junit.platform.engine;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.EventConditions;

import java.util.Optional;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
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
    void selectAndExecuteNoScenario() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(0, event(test()));
    }

    @Test
    void selectAndExecuteSingleScenario() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(2, event(test()))
                .haveExactly(1, event(finishedSuccessfully()));
    }

    @Test
    void selectAndExecuteSingleScenarioThroughFeaturesProperty() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .configurationParameter(FEATURES_PROPERTY_NAME,
                    "src/test/resources/io/cucumber/junit/platform/engine/single.feature")
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(engine(source(ClassSource.from(CucumberTestEngine.class)))))
                .haveExactly(1, event(test(finishedSuccessfully())));
    }

    @Test
    void selectAndExecuteSingleScenarioWithoutFeaturesProperty() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .allEvents()
                .assertThatEvents()
                .haveExactly(2, event(engine(emptySource())))
                .haveExactly(1, event(test(finishedSuccessfully())));
    }

    @Test
    void selectAndSkipDisabledScenarioByTags() {
        EngineTestKit.engine(ENGINE_ID)
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
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
                .configurationParameter(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true")
                .configurationParameter(FILTER_NAME_PROPERTY_NAME, "^Nothing$")
                .selectors(selectFile("src/test/resources/io/cucumber/junit/platform/engine/single.feature"))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(1, event(test()))
                .haveExactly(1,
                    event(skippedWithReason("'cucumber.filter.name=^Nothing$' did not match this scenario")));
    }

    private static Condition<Event> engine(Condition<Event> condition) {
        return Assertions.allOf(EventConditions.engine(), condition);
    }

    private static Condition<Event> source(TestSource testSource) {
        return new Condition<>(event -> event.getTestDescriptor().getSource().filter(testSource::equals).isPresent(),
            "test engine with test source '%s'", testSource);
    }

    private static Condition<Event> emptySource() {
        return new Condition<>(event -> !event.getTestDescriptor().getSource().isPresent(), "without a test source");
    }

}
