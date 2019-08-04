package io.cucumber.core.plugin;

import io.cucumber.core.event.SnippetsSuggestedEvent;
import io.cucumber.core.event.TestSourceRead;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.time.Duration.ZERO;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UndefinedStepsTrackerTest {

    @Test
    public void has_undefined_steps() {
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.handleSnippetsSuggested(uri(), locations(), asList(""));
        assertTrue(undefinedStepsTracker.hasUndefinedSteps());
    }

    @Test
    public void has_no_undefined_steps() {
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        assertFalse(undefinedStepsTracker.hasUndefinedSteps());
    }

    @Test
    public void removes_duplicates() {
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.handleSnippetsSuggested(uri(), locations(), asList("**KEYWORD** ^B$"));
        tracker.handleSnippetsSuggested(uri(), locations(), asList("**KEYWORD** ^B$"));
        assertThat(tracker.getSnippets().toString(), is(equalTo("[Given ^B$]")));
    }

    @Test
    public void uses_given_when_then_keywords() {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given A\n" +
            "    Then B\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(4)), asList("**KEYWORD** ^B$"));
        assertThat(tracker.getSnippets().toString(), is(equalTo("[Then ^B$]")));
    }

    @Test
    public void converts_and_to_previous_step_keyword() {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    When A\n" +
            "    And B\n" +
            "    But C\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(5)), asList("**KEYWORD** ^C$"));
        assertThat(tracker.getSnippets().toString(), is(equalTo("[When ^C$]")));
    }

    @Test
    public void backtrack_into_background_to_find_step_keyword() {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background:\n" +
            "    When A\n" +
            "  Scenario: scenario name\n" +
            "    And B\n" +
            "    But C\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(5)), asList("**KEYWORD** ^C$"));
        assertThat(tracker.getSnippets().toString(), is(equalTo("[When ^C$]")));
    }

    private void sendTestSourceRead(EventBus bus, CucumberFeature feature) {
        bus.send(new TestSourceRead(bus.getInstant(), feature.getUri().toString(), feature.getSource()));
    }

    @Test
    public void doesnt_try_to_use_star_keyword() {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    When A\n" +
            "    And B\n" +
            "    * C\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(5)), asList("**KEYWORD** ^C$"));
        assertThat(tracker.getSnippets().toString(), is(equalTo("[When ^C$]")));
    }

    @Test
    public void star_keyword_becomes_given_when_no_previous_step() {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    * A\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("path/test.feature"), locations(line(3)), asList("**KEYWORD** ^A$"));
        assertThat(tracker.getSnippets().toString(), is(equalTo("[Given ^A$]")));
    }

    @Test
    public void snippets_are_generated_for_correct_locale() {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "#language:ru\n" +
            "Функция:\n" +
            "  Сценарий: \n" +
            "    * Б\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(4)), asList("**KEYWORD** ^Б$"));
        assertThat(tracker.getSnippets().toString(), is(equalTo("[Допустим ^Б$]")));
    }

    private List<SnippetsSuggestedEvent.Location> locations(int line) {
        return asList(new SnippetsSuggestedEvent.Location(line, 0));
    }

    private List<SnippetsSuggestedEvent.Location> locations() {
        return Collections.emptyList();
    }

    private String uri() {
        return uri("");
    }

    private String uri(String path) {
        return path;
    }

    private int line(int line) {
        return line;
    }

}
