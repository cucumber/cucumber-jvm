package cucumber.api.event;

import cucumber.api.TestCase;
import gherkin.pickles.PickleLocation;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CanonicalEventOrderTest {

    private static final int LESS_THAN = -1;
    private static final int EQUAL_TO = 0;
    private static final int GREATER_THAN = 1;

    private CanonicalEventOrder comparator = new CanonicalEventOrder();

    static long getTime() {
        return new Date().getTime();
    }

    static Event createTestCaseEvent(final String uri, final int line) {
        final TestCase testCase = mock(TestCase.class);
        given(testCase.getUri()).willReturn(uri);
        given(testCase.getLine()).willReturn(line);
        return new TestCaseStarted(getTime(), testCase);
    }

    private Event runStarted = new TestRunStarted(getTime());
    private Event testRead = new TestSourceRead(getTime(), "uri", "source");
    private Event suggested = new SnippetsSuggestedEvent(getTime(), "uri", Collections.<PickleLocation>emptyList(), Collections.<String>emptyList());
    private Event feature1Case1Started = createTestCaseEvent("feature1", 1);
    private Event feature1Case2Started = createTestCaseEvent("feature1", 9);
    private Event feature1Case3Started = createTestCaseEvent("feature1", 11);
    private Event feature2Case1Started = createTestCaseEvent("feature2", 1);
    private Event runFinished = new TestRunFinished(getTime());

    @Test
    public void verifyTestRunStartedSortedCorrectly() {
        assertThat(comparator.compare(runStarted, runStarted)).isEqualTo(EQUAL_TO);
        assertThat(comparator.compare(runStarted, testRead)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(runStarted, suggested)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(runStarted, feature1Case1Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(runStarted, feature1Case2Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(runStarted, feature1Case3Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(runStarted, feature2Case1Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(runStarted, runFinished)).isEqualTo(LESS_THAN);
    }

    @Test
    public void verifyTestSourceReadSortedCorrectly() {
        assertThat(comparator.compare(testRead, runStarted)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(testRead, testRead)).isEqualTo(EQUAL_TO);
        assertThat(comparator.compare(testRead, suggested)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(testRead, feature1Case1Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(testRead, feature1Case2Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(testRead, feature1Case3Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(testRead, feature2Case1Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(testRead, runFinished)).isEqualTo(LESS_THAN);
    }

    @Test
    public void verifySnippetsSuggestedSortedCorrectly() {
        assertThat(comparator.compare(suggested, runStarted)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(suggested, testRead)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(suggested, suggested)).isEqualTo(EQUAL_TO);
        assertThat(comparator.compare(suggested, feature1Case1Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(suggested, feature1Case2Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(suggested, feature1Case3Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(suggested, feature2Case1Started)).isEqualTo(LESS_THAN);
        assertThat(comparator.compare(suggested, runFinished)).isEqualTo(LESS_THAN);
    }

    @Test
    public void verifyTestCaseStartedSortedCorrectly() {
        final List<Event> greaterThan = Arrays.asList(runStarted, testRead, suggested);
        for (final Event e : greaterThan) {
            assertThat(comparator.compare(feature1Case1Started, e)).isEqualTo(GREATER_THAN);
            assertThat(comparator.compare(feature1Case2Started, e)).isEqualTo(GREATER_THAN);
            assertThat(comparator.compare(feature1Case3Started, e)).isEqualTo(GREATER_THAN);
            assertThat(comparator.compare(feature2Case1Started, e)).isEqualTo(GREATER_THAN);
        }

        final List<Event> lessThan = Collections.singletonList(runFinished);
        for (final Event e : lessThan) {
            assertThat(comparator.compare(feature1Case1Started, e)).isEqualTo(LESS_THAN);
            assertThat(comparator.compare(feature1Case2Started, e)).isEqualTo(LESS_THAN);
            assertThat(comparator.compare(feature1Case3Started, e)).isEqualTo(LESS_THAN);
            assertThat(comparator.compare(feature2Case1Started, e)).isEqualTo(LESS_THAN);
        }

        assertThat(comparator.compare(feature1Case1Started, feature1Case2Started)).isLessThan(EQUAL_TO);
        assertThat(comparator.compare(feature1Case2Started, feature1Case3Started)).isLessThan(EQUAL_TO);
        assertThat(comparator.compare(feature1Case3Started, feature2Case1Started)).isLessThan(EQUAL_TO);

    }

    @Test
    public void verifyTestRunFinishedSortedCorrectly() {
        assertThat(comparator.compare(runFinished, runStarted)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(runFinished, suggested)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(runFinished, testRead)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(runFinished, feature1Case1Started)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(runFinished, feature1Case2Started)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(runFinished, feature1Case3Started)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(runFinished, feature2Case1Started)).isEqualTo(GREATER_THAN);
        assertThat(comparator.compare(runFinished, runFinished)).isEqualTo(EQUAL_TO);
    }
}