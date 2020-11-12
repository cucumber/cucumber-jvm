package io.cucumber.core.plugin;

import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.TestSourceRead;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.Instant.ofEpochMilli;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CanonicalEventOrderTest {

    private static final int EQUAL_TO = 0;

    private final CanonicalEventOrder comparator = new CanonicalEventOrder();
    private final Event runStarted = new TestRunStarted(ofEpochMilli(0));
    private final Event testRead = new TestSourceRead(
        ofEpochMilli(1),
        URI.create("file:path/to.feature"),
        "source");
    private final Event testParsed = new TestSourceParsed(
        ofEpochMilli(3),
        URI.create("file:path/to.feature"),
        Collections.emptyList());
    private final Event suggested = new SnippetsSuggestedEvent(
        ofEpochMilli(4),
        URI.create("file:path/to/1.feature"),
        new Location(0, -1),
        new Location(0, -1),
        Collections.emptyList());
    private final Event suggested2 = new SnippetsSuggestedEvent(
        ofEpochMilli(5),
        URI.create("file:path/to/1.feature"),
        new Location(0, -1),
        new Location(0, -1),
        Collections.emptyList());
    private final Event feature1Case1Started = createTestCaseEvent(
        ofEpochMilli(5),
        URI.create("file:path/to/1.feature"),
        1);
    private final Event feature1Case1Started2 = createTestCaseEvent(
        ofEpochMilli(6),
        URI.create("file:path/to/1.feature"),
        1);
    private final Event feature1Case2Started = createTestCaseEvent(
        ofEpochMilli(5),
        URI.create("file:path/to/1.feature"),
        9);
    private final Event feature1Case3Started = createTestCaseEvent(
        ofEpochMilli(6),
        URI.create("file:path/to/1.feature"),
        11);
    private final Event feature2Case1Started = createTestCaseEvent(
        ofEpochMilli(5),
        URI.create("file:path/to/2.feature"),
        1);
    private final Event runFinished = new TestRunFinished(
        ofEpochMilli(7),
        new Result(Status.PASSED, Duration.ZERO, null));

    private static TestCaseStarted createTestCaseEvent(Instant instant, URI uri, int line) {
        final TestCase testCase = mock(TestCase.class);
        given(testCase.getUri()).willReturn(uri);
        given(testCase.getLocation()).willReturn(new Location(line, -1));
        return new TestCaseStarted(instant, testCase);
    }

    @Test
    void verifyTestRunStartedSortedCorrectly() {
        assertAll(
            () -> assertThat(comparator.compare(runStarted, runStarted), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, testRead), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, testParsed), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, suggested), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, feature1Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, feature1Case2Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, feature1Case3Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, feature2Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, runFinished), lessThan(EQUAL_TO)));
    }

    @Test
    void verifyTestSourceReadSortedCorrectly() {
        assertAll(
            () -> assertThat(comparator.compare(testRead, runStarted), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, testRead), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, testParsed), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, suggested), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, feature1Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, feature1Case2Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, feature1Case3Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, feature2Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, runFinished), lessThan(EQUAL_TO)));
    }

    @Test
    void verifyTestSourceParsedSortedCorrectly() {
        assertAll(
            () -> assertThat(comparator.compare(testParsed, runStarted), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, testRead), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, testParsed), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, suggested), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, feature1Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, feature1Case2Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, feature1Case3Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, feature2Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(testParsed, runFinished), lessThan(EQUAL_TO)));
    }

    @Test
    void verifySnippetsSuggestedSortedCorrectly() {
        assertAll(
            () -> assertThat(comparator.compare(suggested, runStarted), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, testRead), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, testParsed), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, suggested), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, suggested2), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, feature1Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, feature1Case2Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, feature1Case3Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, feature2Case1Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, runFinished), lessThan(EQUAL_TO)));
    }

    @Test
    void verifyTestCaseStartedSortedCorrectly() {
        final List<Event> greaterThan = Arrays.asList(runStarted, testRead, suggested);
        for (final Event e : greaterThan) {
            assertAll(
                () -> assertThat(comparator.compare(feature1Case1Started, e), greaterThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(feature1Case2Started, e), greaterThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(feature1Case3Started, e), greaterThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(feature2Case1Started, e), greaterThan(EQUAL_TO)));
        }

        final List<Event> lessThan = Collections.singletonList(runFinished);
        for (final Event e : lessThan) {
            assertAll(
                () -> assertThat(comparator.compare(feature1Case1Started, e), lessThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(feature1Case2Started, e), lessThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(feature1Case3Started, e), lessThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(feature2Case1Started, e), lessThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(e, feature1Case1Started), greaterThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(e, feature1Case2Started), greaterThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(e, feature1Case3Started), greaterThan(EQUAL_TO)),
                () -> assertThat(comparator.compare(e, feature2Case1Started), greaterThan(EQUAL_TO)));
        }

        assertAll(
            () -> assertThat(comparator.compare(feature1Case1Started, feature1Case1Started), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(feature1Case1Started, feature1Case1Started2), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(feature1Case1Started, feature1Case2Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(feature1Case1Started, feature1Case2Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(feature1Case2Started, feature1Case3Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(feature1Case3Started, feature2Case1Started), lessThan(EQUAL_TO)));
    }

    @Test
    void verifyTestRunFinishedSortedCorrectly() {
        assertAll(
            () -> assertThat(comparator.compare(runFinished, runStarted), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, suggested), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, testRead), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, testParsed), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, feature1Case1Started), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, feature1Case2Started), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, feature1Case3Started), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, feature2Case1Started), greaterThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(runFinished, runFinished), equalTo(EQUAL_TO)));
    }

}
