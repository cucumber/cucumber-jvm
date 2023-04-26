package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmbiguousStepDefinitionMatchTest {

    private final Feature feature = TestFeatureParser.parse("file:test.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final Step step = feature.getPickles().get(0).getSteps().get(0);
    private final AmbiguousStepDefinitionsException e = new AmbiguousStepDefinitionsException(step, emptyList());
    private final AmbiguousPickleStepDefinitionsMatch match = new AmbiguousPickleStepDefinitionsMatch(
        URI.create("file:path/to.feature"), step, e);
    private final TestCaseState mockTestCaseState = new TestCaseState(new MockEventBus(),
        new IncrementingUuidGenerator().generateId(), new MockTestCase());

    @Test
    void throws_ambiguous_step_definitions_exception_when_run() {
        Executable testMethod = () -> match.runStep(mockTestCaseState);
        AmbiguousStepDefinitionsException actualThrown = assertThrows(AmbiguousStepDefinitionsException.class,
            testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "\"I have 4 cukes in my belly\" matches more than one step definition:\n")));
    }

    @Test
    void throws_ambiguous_step_definitions_exception_when_dry_run() {
        Executable testMethod = () -> match.dryRunStep(mockTestCaseState);
        AmbiguousStepDefinitionsException actualThrown = assertThrows(AmbiguousStepDefinitionsException.class,
            testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "\"I have 4 cukes in my belly\" matches more than one step definition:\n")));
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
