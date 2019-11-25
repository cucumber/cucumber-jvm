package io.cucumber.core.plugin;

import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CanonicalEventOrderTest {

    private static final int LESS_THAN = -1;
    private static final int EQUAL_TO = 0;
    private static final int GREATER_THAN = 1;

    private CanonicalEventOrder comparator = new CanonicalEventOrder();

    private static Instant getInstant() {
        return Instant.now();
    }

    private static Event createTestCaseEvent(final URI uri, final int line) {
        final TestCase testCase = mock(TestCase.class);
        given(testCase.getUri()).willReturn(uri);
        given(testCase.getLine()).willReturn(line);
        return new TestCaseStarted(getInstant(), testCase);
    }

    private Event runStarted = new TestRunStarted(getInstant());
    private Event testRead = new TestSourceRead(getInstant(), URI.create("file:path/to.feature"), "source");
    private Event suggested = new SnippetsSuggestedEvent(getInstant(), URI.create("file:path/to/1.feature"), 0, Collections.emptyList());
    private Event feature1Case1Started = createTestCaseEvent(URI.create("file:path/to/1.feature"), 1);
    private Event feature1Case2Started = createTestCaseEvent(URI.create("file:path/to/1.feature"), 9);
    private Event feature1Case3Started = createTestCaseEvent(URI.create("file:path/to/1.feature"), 11);
    private Event feature2Case1Started = createTestCaseEvent(URI.create("file:path/to/2.feature"), 1);
    private Event runFinished = new TestRunFinished(getInstant());

    @Test
    void verifyTestRunStartedSortedCorrectly() {
        assertAll("comparator CanonicalEventOrder",
            () -> assertThat(comparator.compare(runStarted, runStarted), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(runStarted, testRead), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(runStarted, suggested), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(runStarted, feature1Case1Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(runStarted, feature1Case2Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(runStarted, feature1Case3Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(runStarted, feature2Case1Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(runStarted, runFinished), equalTo(LESS_THAN))
        );
    }

    @Test
    void verifyTestSourceReadSortedCorrectly() {
        assertAll("comparator CanonicalEventOrder",
            () -> assertThat(comparator.compare(testRead, runStarted), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(testRead, testRead), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(testRead, suggested), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(testRead, feature1Case1Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(testRead, feature1Case2Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(testRead, feature1Case3Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(testRead, feature2Case1Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(testRead, runFinished), equalTo(LESS_THAN))
        );
    }

    @Test
    void verifySnippetsSuggestedSortedCorrectly() {
        assertAll("comparator CanonicalEventOrder",
            () -> assertThat(comparator.compare(suggested, runStarted), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(suggested, testRead), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(suggested, suggested), equalTo(EQUAL_TO)),
            () -> assertThat(comparator.compare(suggested, feature1Case1Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(suggested, feature1Case2Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(suggested, feature1Case3Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(suggested, feature2Case1Started), equalTo(LESS_THAN)),
            () -> assertThat(comparator.compare(suggested, runFinished), equalTo(LESS_THAN))
        );
    }

    @Test
    void verifyTestCaseStartedSortedCorrectly() {
        final List<Event> greaterThan = Arrays.asList(runStarted, testRead, suggested);
        for (final Event e : greaterThan) {
            assertAll("comparator CanonicalEventOrder",
                () -> assertThat(comparator.compare(feature1Case1Started, e), equalTo(GREATER_THAN)),
                () -> assertThat(comparator.compare(feature1Case2Started, e), equalTo(GREATER_THAN)),
                () -> assertThat(comparator.compare(feature1Case3Started, e), equalTo(GREATER_THAN)),
                () -> assertThat(comparator.compare(feature2Case1Started, e), equalTo(GREATER_THAN))
            );
        }

        final List<Event> lessThan = Collections.singletonList(runFinished);
        for (final Event e : lessThan) {
            assertAll("comparator CanonicalEventOrder",
                () -> assertThat(comparator.compare(feature1Case1Started, e), equalTo(LESS_THAN)),
                () -> assertThat(comparator.compare(feature1Case2Started, e), equalTo(LESS_THAN)),
                () -> assertThat(comparator.compare(feature1Case3Started, e), equalTo(LESS_THAN)),
                () -> assertThat(comparator.compare(feature2Case1Started, e), equalTo(LESS_THAN))
            );
        }

        assertAll("comparator CanonicalEventOrder",
            () -> assertThat(comparator.compare(feature1Case1Started, feature1Case2Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(feature1Case2Started, feature1Case3Started), lessThan(EQUAL_TO)),
            () -> assertThat(comparator.compare(feature1Case3Started, feature2Case1Started), lessThan(EQUAL_TO))
        );
    }

    @Test
    void verifyTestRunFinishedSortedCorrectly() {
        assertAll("comparator CanonicalEventOrder",
            () -> assertThat(comparator.compare(runFinished, runStarted), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(runFinished, suggested), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(runFinished, testRead), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(runFinished, feature1Case1Started), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(runFinished, feature1Case2Started), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(runFinished, feature1Case3Started), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(runFinished, feature2Case1Started), equalTo(GREATER_THAN)),
            () -> assertThat(comparator.compare(runFinished, runFinished), equalTo(EQUAL_TO))
        );
    }

}
