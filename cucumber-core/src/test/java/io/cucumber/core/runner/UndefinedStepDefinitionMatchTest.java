package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UndefinedStepDefinitionMatchTest {

    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");

    private final UndefinedPickleStepDefinitionMatch match = new UndefinedPickleStepDefinitionMatch(
        URI.create("file:path/to.feature"),
        feature.getPickles().get(0).getSteps().get(0));
    private final TestCaseState mockTestCaseState = new TestCaseState(new MockEventBus(),
        new IncrementingUuidGenerator().generateId(), new MockTestCase());
    @Test
    void throws_undefined_step_definitions_exception_when_run() {
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class,
            () -> match.runStep(mockTestCaseState));
        assertThat(expectedThrown.getMessage(), equalTo("No step definitions found"));
    }

    @Test
    void throws_undefined_step_definitions_exception_when_dry_run() {
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class,
            () -> match.dryRunStep(mockTestCaseState));
        assertThat(expectedThrown.getMessage(), equalTo("No step definitions found"));
    }

    private static class MockTestCase implements TestCase {
        @Override
        public Integer getLine() {
            return null;
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public String getKeyword() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getScenarioDesignation() {
            return null;
        }

        @Override
        public List<String> getTags() {
            return null;
        }

        @Override
        public List<TestStep> getTestSteps() {
            return null;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }
    }

    private static class MockEventBus implements EventBus {
        @Override
        public Instant getInstant() {
            return null;
        }

        @Override
        public UUID generateId() {
            return null;
        }

        @Override
        public <T> void send(T event) {

        }

        @Override
        public <T> void sendAll(Iterable<T> queue) {

        }

        @Override
        public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }

        @Override
        public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }
    }

}
