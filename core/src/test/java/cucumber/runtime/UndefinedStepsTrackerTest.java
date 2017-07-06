package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.runner.EventBus;
import cucumber.runner.TimeServiceStub;
import cucumber.runtime.model.CucumberFeature;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.Argument;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UndefinedStepsTrackerTest {

    @Test
    public void has_undefined_steps() {
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.handleTestStepFinished(testStep(), undefinedResultWithSnippets(asList("")));
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
        tracker.handleTestStepFinished(testStep(), undefinedResultWithSnippets(asList("**KEYWORD** ^B$")));
        tracker.handleTestStepFinished(testStep(), undefinedResultWithSnippets(asList("**KEYWORD** ^B$")));
        assertEquals("[Given ^B$]", tracker.getSnippets().toString());
    }

    @Test
    public void uses_given_when_then_keywords() throws IOException {
        EventBus bus = new EventBus(new TimeServiceStub(0));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given A\n" +
                "    Then B\n");
        feature.sendTestSourceRead(bus);
        tracker.handleTestCaseStarted(testCase(path("path/test.feature")));
        tracker.handleTestStepFinished(testStep(line(4)), undefinedResultWithSnippets(asList("**KEYWORD** ^B$")));
        assertEquals("[Then ^B$]", tracker.getSnippets().toString());
    }

    @Test
    public void converts_and_to_previous_step_keyword() throws IOException {
        EventBus bus = new EventBus(new TimeServiceStub(0));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    When A\n" +
                "    And B\n" +
                "    But C\n");
        feature.sendTestSourceRead(bus);
        tracker.handleTestCaseStarted(testCase(path("path/test.feature")));
        tracker.handleTestStepFinished(testStep(line(5)), undefinedResultWithSnippets(asList("**KEYWORD** ^C$")));
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    @Test
    public void backtrack_into_background_to_find_step_keyword() throws IOException {
        EventBus bus = new EventBus(new TimeServiceStub(0));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background:\n" +
                "    When A\n" +
                "  Scenario: scenario name\n" +
                "    And B\n" +
                "    But C\n");
        feature.sendTestSourceRead(bus);
        tracker.handleTestCaseStarted(testCase(path("path/test.feature")));
        tracker.handleTestStepFinished(testStep(line(5)), undefinedResultWithSnippets(asList("**KEYWORD** ^C$")));
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    @Test
    public void doesnt_try_to_use_star_keyword() throws IOException {
        EventBus bus = new EventBus(new TimeServiceStub(0));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    When A\n" +
                "    And B\n" +
                "    * C\n");
        feature.sendTestSourceRead(bus);
        tracker.handleTestCaseStarted(testCase(path("path/test.feature")));
        tracker.handleTestStepFinished(testStep(line(5)), undefinedResultWithSnippets(asList("**KEYWORD** ^C$")));
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    @Test
    public void star_keyword_becomes_given_when_no_previous_step() throws IOException {
        EventBus bus = new EventBus(new TimeServiceStub(0));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    * A\n");
        feature.sendTestSourceRead(bus);
        tracker.handleTestCaseStarted(testCase(path("path/test.feature")));
        tracker.handleTestStepFinished(testStep(line(3)), undefinedResultWithSnippets(asList("**KEYWORD** ^A$")));
        assertEquals("[Given ^A$]", tracker.getSnippets().toString());
    }

    @Test
    public void snippets_are_generated_for_correct_locale() throws Exception {
        EventBus bus = new EventBus(new TimeServiceStub(0));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "#language:ru\n" +
                "Функция:\n" +
                "  Сценарий: \n" +
                "    * Б\n");
        feature.sendTestSourceRead(bus);
        tracker.handleTestCaseStarted(testCase(path("path/test.feature")));
        tracker.handleTestStepFinished(testStep(line(4)), undefinedResultWithSnippets(asList("**KEYWORD** ^Б$")));
        assertEquals("[Допустим ^Б$]", tracker.getSnippets().toString());
    }

    private TestCase testCase(String path) {
        TestCase testCase = mock(TestCase.class);
        when(testCase.getPath()).thenReturn(path);
        return testCase;
    }

    private TestStep testStep(int line) {
        return testStep(asList(new PickleLocation(line, 0)));
    }

    private TestStep testStep() {
        return testStep(Collections.<PickleLocation>emptyList());
    }

    private TestStep testStep(List<PickleLocation> locations) {
        TestStep testStep = mock(TestStep.class);
        PickleStep pickleStep = new PickleStep("step text", Collections.<Argument>emptyList(), locations);
        when(testStep.getPickleStep()).thenReturn(pickleStep);
        return testStep;
    }

    private String path(String path) {
        return path;
    }

    private int line(int line) {
        return line;
    }

    private Result undefinedResultWithSnippets(List<String> snippets) {
        Result result = mock(Result.class);
        when(result.is(Result.Type.UNDEFINED)).thenReturn(true);
        when(result.getSnippets()).thenReturn(snippets);
        return result;
    }

}
