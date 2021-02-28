package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.Messages.Attachment.ContentEncoding;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.plugin.event.EmbedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCaseStateTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    @Test
    void provides_the_uri_of_the_feature_file() {
        Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        TestCaseState state = createTestCaseState(feature);
        assertThat(state.getUri(), is(new File("path/file.feature").toURI()));
    }

    private TestCaseState createTestCaseState(Feature feature) {
        return new TestCaseState(bus,
            UUID.randomUUID(),
            new TestCase(
                UUID.randomUUID(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                feature.getPickles().get(0),
                false));
    }

    @Test
    void provides_the_scenario_line() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");

        TestCaseState state = createTestCaseState(feature);
        assertThat(state.getLine(), is(2));
    }

    @Test
    void provides_both_the_example_row_line_and_scenario_outline_line_for_scenarios_from_scenario_outlines() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario Outline: Test scenario\n" +
                "     Given I have 4 <thing> in my belly\n" +
                "     Examples:\n" +
                "       | thing | \n" +
                "       | cuke  | \n");

        TestCaseState state = createTestCaseState(feature);
        assertThat(state.getLine(), is(6));
    }

    @Test
    void provides_the_uri_and_scenario_line_as_unique_id() {
        Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");

        TestCaseState state = createTestCaseState(feature);

        assertThat(state.getId(), is(new File("path/file.feature:2").toURI().toString()));
    }

    @Test
    void provides_the_uri_and_example_row_line_as_unique_id_for_scenarios_from_scenario_outlines() {
        Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario Outline: Test scenario\n" +
                "     Given I have 4 <thing> in my belly\n" +
                "     Examples:\n" +
                "       | thing | \n" +
                "       | cuke  | \n");
        TestCaseState state = createTestCaseState(feature);

        assertThat(state.getId(), is(new File("path/file.feature:6").toURI().toString()));
    }

    @Test
    void attach_bytes_emits_event_on_bus() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        TestCaseState state = createTestCaseState(feature);

        List<EmbedEvent> embedEvents = new ArrayList<>();
        List<Envelope> envelopes = new ArrayList<>();
        bus.registerHandlerFor(EmbedEvent.class, embedEvents::add);
        bus.registerHandlerFor(Envelope.class, envelopes::add);

        UUID activeTestStep = UUID.randomUUID();
        state.setCurrentTestStepId(activeTestStep);
        state.attach("Hello World".getBytes(UTF_8), "text/plain", "hello.txt");

        EmbedEvent embedEvent = embedEvents.get(0);
        assertThat(embedEvent.getData(), is("Hello World".getBytes(UTF_8)));
        assertThat(embedEvent.getMediaType(), is("text/plain"));
        assertThat(embedEvent.getName(), is("hello.txt"));

        Envelope envelope = envelopes.get(0);
        assertThat(envelope.getAttachment().getBody(),
            is(Base64.getEncoder().encodeToString("Hello World".getBytes(UTF_8))));
        assertThat(envelope.getAttachment().getContentEncoding(), is(ContentEncoding.BASE64));
        assertThat(envelope.getAttachment().getMediaType(), is("text/plain"));
        assertThat(envelope.getAttachment().getFileName(), is("hello.txt"));
        assertThat(envelope.getAttachment().getTestStepId(), is(activeTestStep.toString()));
        assertThat(envelope.getAttachment().getTestCaseStartedId(), is(state.getTestExecutionId().toString()));
    }

    @Test
    void attach_string_emits_event_on_bus() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        TestCaseState state = createTestCaseState(feature);

        List<EmbedEvent> embedEvents = new ArrayList<>();
        List<Envelope> envelopes = new ArrayList<>();
        bus.registerHandlerFor(EmbedEvent.class, embedEvents::add);
        bus.registerHandlerFor(Envelope.class, envelopes::add);

        UUID activeTestStep = UUID.randomUUID();
        state.setCurrentTestStepId(activeTestStep);
        state.attach("Hello World", "text/plain", "hello.txt");

        EmbedEvent embedEvent = embedEvents.get(0);
        assertThat(embedEvent.getData(), is("Hello World".getBytes(UTF_8)));
        assertThat(embedEvent.getMediaType(), is("text/plain"));
        assertThat(embedEvent.getName(), is("hello.txt"));

        Envelope envelope = envelopes.get(0);
        assertThat(envelope.getAttachment().getBody(), is("Hello World"));
        assertThat(envelope.getAttachment().getContentEncoding(), is(ContentEncoding.IDENTITY));
        assertThat(envelope.getAttachment().getMediaType(), is("text/plain"));
        assertThat(envelope.getAttachment().getFileName(), is("hello.txt"));
        assertThat(envelope.getAttachment().getTestStepId(), is(activeTestStep.toString()));
        assertThat(envelope.getAttachment().getTestCaseStartedId(), is(state.getTestExecutionId().toString()));
    }

    @Test
    void attach_throws_when_test_step_is_not_active() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        TestCaseState state = createTestCaseState(feature);

        assertThrows(IllegalStateException.class, () -> state.attach("Hello World", "text/plain", "hello.txt"));
        assertThrows(IllegalStateException.class,
            () -> state.attach("Hello World".getBytes(UTF_8), "text/plain", "hello.txt"));
        assertThrows(IllegalStateException.class, () -> state.log("Hello World"));
    }

}
