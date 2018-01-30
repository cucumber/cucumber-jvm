package cucumber.runtime.formatter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Instrumentation;
import android.os.Bundle;
import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.TestSourceRead;
import cucumber.runtime.Runtime;
import cucumber.runtime.formatter.AndroidInstrumentationReporter.StatusCodes;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AndroidInstrumentationReporterTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final Runtime runtime = mock(Runtime.class);
    private final Instrumentation instrumentation = mock(Instrumentation.class);

    private final TestSourceRead testSourceRead = new TestSourceRead(
        0l,
        "path/file.feature",
        "Feature: feature name\n  Scenario: some important scenario\n");
    private final TestCase testCase = mock(TestCase.class);
    private final Result firstResult = mock(Result.class);
    private final Result secondResult = mock(Result.class);


    @Before
    public void beforeEachTest() {
        when(testCase.getUri()).thenReturn("path/file.feature");
        when(testCase.getName()).thenReturn("Some important scenario");
    }

    @Test
    public void feature_name_and_keyword_is_contained_in_start_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);

        // when
        formatter.testSourceRead(testSourceRead);
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.START), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString("feature name"));
    }

    @Test
    public void feature_name_and_keyword_is_contained_in_end_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);

        // when
        formatter.testSourceRead(testSourceRead);
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.OK), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString("feature name"));
    }

    @Test
    public void scenario_name_is_contained_in_start_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.START), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(testCase.getName()));
    }

    @Test
    public void scenario_name_is_contained_in_end_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.OK), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(testCase.getName()));
    }

    @Test
    public void any_step_exception_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("some random runtime exception"));

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some random runtime exception"));

    }

    @Test
    public void any_failing_step_causes_test_failure() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.FAILED);
        when(firstResult.getError()).thenReturn(new AssertionError("some test assertion went wrong"));
        when(firstResult.getErrorMessage()).thenReturn("some test assertion went wrong");

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some test assertion went wrong"));
    }

    @Test
    public void any_undefined_step_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.UNDEFINED);
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void passing_step_causes_test_success() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        verify(instrumentation).sendStatus(eq(StatusCodes.OK), any(Bundle.class));
    }

    @Test
    public void skipped_step_causes_test_success() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);
        mockResultStatus(secondResult, Result.Type.SKIPPED);

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        verify(instrumentation).sendStatus(eq(StatusCodes.OK), any(Bundle.class));

    }

    @Test
    public void first_step_result_exception_is_reported() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("first exception"));

        mockResultStatus(secondResult, Result.Type.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("second exception"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("first exception"));
    }

    @Test
    public void undefined_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);

        mockResultStatus(secondResult, Result.Type.UNDEFINED);
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void pending_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);

        mockResultStatus(secondResult, Result.Type.PENDING);
        when(secondResult.getError()).thenReturn(new PendingException("step is pending"));
        when(secondResult.getErrorMessage()).thenReturn("step is pending");

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("step is pending"));
    }

    @Test
    public void failed_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);

        mockResultStatus(secondResult, Result.Type.FAILED);
        when(secondResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(secondResult.getErrorMessage()).thenReturn("some assertion went wrong");

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some assertion went wrong"));
    }

    @Test
    public void error_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);

        mockResultStatus(secondResult, Result.Type.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("some exception"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some exception"));
    }

    @Test
    public void failed_step_does_not_overrides_preceding_undefined_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.UNDEFINED);
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        mockResultStatus(secondResult, Result.Type.FAILED);
        when(secondResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(secondResult.getErrorMessage()).thenReturn("some assertion went wrong");

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void error_step_does_not_override_preceding_failed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.FAILED);
        when(firstResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(firstResult.getErrorMessage()).thenReturn("some assertion went wrong");

        mockResultStatus(secondResult, Result.Type.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("some exception"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some assertion went wrong"));
    }

    @Test
    public void step_result_contains_only_the_current_scenarios_severest_result() {
        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.FAILED);
        when(firstResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(firstResult.getErrorMessage()).thenReturn("some assertion went wrong");

        mockResultStatus(secondResult, Result.Type.PASSED);

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        formatter.startTestCase(testCase);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then

        final InOrder inOrder = inOrder(instrumentation);
        final ArgumentCaptor<Bundle> firstCaptor = ArgumentCaptor.forClass(Bundle.class);
        final ArgumentCaptor<Bundle> secondCaptor = ArgumentCaptor.forClass(Bundle.class);

        inOrder.verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), firstCaptor.capture());
        inOrder.verify(instrumentation).sendStatus(eq(StatusCodes.OK), secondCaptor.capture());
    }

    /**
     * Verifies, that different test names are used when scenario names are duplicated.
     * More technical: Checks, if {{@link AndroidInstrumentationReporter#getUniqueTestName(TestCase)}} is working.
     *
     * Note about the test code: We are using multiple assertions and for-loops in this method, which is a code smell.
     * Here it is okay, since we are just answering the question if the getUniqueTestName-method is working or not.
     */
    @Test
    public void scenario_outline_all_test_names_unique() {
        // given
        String[] featureNames = new String[] {"Addition", "Subtraction", "Multiplication", "Division"};
        String[] scenarioNames = new String[] {"Enter one number", "Enter two numbers"};
        List<UniqueTestNameTester> uniqueTestNameTesters = new ArrayList<UniqueTestNameTester>();
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);
        for (String featureName : featureNames) {
            for (String scenarioName : scenarioNames) {
                for (int exampleIndex = 0; exampleIndex < 3; exampleIndex++) {
                    // We use the same scenario name three times in every feature.
                    // In practise that happens on scenario outlines.
                    uniqueTestNameTesters.add(new UniqueTestNameTester(formatter, featureName, scenarioName));
                }
            }
        }
        // Use scenario name one again for feature one
        uniqueTestNameTesters.add(new UniqueTestNameTester(formatter, featureNames[0], scenarioNames[0]));
        // Use scenario names with underscore
        uniqueTestNameTesters.add(new UniqueTestNameTester(formatter, featureNames[0], "new_scenario"));
        uniqueTestNameTesters.add(new UniqueTestNameTester(formatter, featureNames[0], "new_scenario"));

        // when

        for (UniqueTestNameTester uniqueTestNameTester : uniqueTestNameTesters) {
            uniqueTestNameTester.invokeGetUniqueTestNameOnDifferentTestCaseStates();
        }

        // then

        // Check default behavior
        uniqueTestNameTesters.get(0).assertExpectedUniqueTestName("Enter one number");
        uniqueTestNameTesters.get(1).assertExpectedUniqueTestName("Enter one number 2");
        uniqueTestNameTesters.get(2).assertExpectedUniqueTestName("Enter one number 3");
        uniqueTestNameTesters.get(3).assertExpectedUniqueTestName("Enter two numbers");
        uniqueTestNameTesters.get(4).assertExpectedUniqueTestName("Enter two numbers 2");
        uniqueTestNameTesters.get(5).assertExpectedUniqueTestName("Enter two numbers 3");

        uniqueTestNameTesters.get(6).assertExpectedUniqueTestName("Enter one number");
        uniqueTestNameTesters.get(7).assertExpectedUniqueTestName("Enter one number 2");
        uniqueTestNameTesters.get(8).assertExpectedUniqueTestName("Enter one number 3");
        uniqueTestNameTesters.get(9).assertExpectedUniqueTestName("Enter two numbers");
        uniqueTestNameTesters.get(10).assertExpectedUniqueTestName("Enter two numbers 2");
        uniqueTestNameTesters.get(11).assertExpectedUniqueTestName("Enter two numbers 3");

        uniqueTestNameTesters.get(12).assertExpectedUniqueTestName("Enter one number");
        uniqueTestNameTesters.get(13).assertExpectedUniqueTestName("Enter one number 2");
        uniqueTestNameTesters.get(14).assertExpectedUniqueTestName("Enter one number 3");
        uniqueTestNameTesters.get(15).assertExpectedUniqueTestName("Enter two numbers");
        uniqueTestNameTesters.get(16).assertExpectedUniqueTestName("Enter two numbers 2");
        uniqueTestNameTesters.get(17).assertExpectedUniqueTestName("Enter two numbers 3");

        uniqueTestNameTesters.get(18).assertExpectedUniqueTestName("Enter one number");
        uniqueTestNameTesters.get(19).assertExpectedUniqueTestName("Enter one number 2");
        uniqueTestNameTesters.get(20).assertExpectedUniqueTestName("Enter one number 3");
        uniqueTestNameTesters.get(21).assertExpectedUniqueTestName("Enter two numbers");
        uniqueTestNameTesters.get(22).assertExpectedUniqueTestName("Enter two numbers 2");
        uniqueTestNameTesters.get(23).assertExpectedUniqueTestName("Enter two numbers 3");

        // Check that order of test cases does not matter
        uniqueTestNameTesters.get(24).assertExpectedUniqueTestName("Enter one number 4");

        // Check naming behavior on underscores
        uniqueTestNameTesters.get(25).assertExpectedUniqueTestName("new_scenario");
        uniqueTestNameTesters.get(26).assertExpectedUniqueTestName("new_scenario_2");

        assertThat(uniqueTestNameTesters.size(), equalTo(27));
    }

    /**
     * Our test code for @{{@link AndroidInstrumentationReporter#getUniqueTestName(TestCase)}} is complex. Thus, we
     * create an extra class to allow better readable test method {@link #scenario_outline_all_test_names_unique()}.
     */
    private class UniqueTestNameTester {
        private AndroidInstrumentationReporter formatter;
        private TestCase testCase;

        private String uniqueTestNameOnTestCaseInitialization;
        private String uniqueTestNameAfterTestCaseStarted;
        private String uniqueTestNameAfterTestStepFinished;
        private String uniqueTestNameAfterTestCaseFinished;

        // given
        private UniqueTestNameTester(AndroidInstrumentationReporter formatter, String featureName, String scenarioName) {
            this.formatter = formatter;
            this.testCase = mock(TestCase.class);
            when(testCase.getUri()).thenReturn("path/" + featureName + ".feature");
            when(testCase.getName()).thenReturn(scenarioName);
        }

        // when
        private void invokeGetUniqueTestNameOnDifferentTestCaseStates() {
            // We invoke our method under test ...
            uniqueTestNameOnTestCaseInitialization = formatter.getUniqueTestName(testCase);
            formatter.startTestCase(testCase);
            // ... we invoke the method again after test-case has been started ...
            uniqueTestNameAfterTestCaseStarted = formatter.getUniqueTestName(testCase);
            formatter.finishTestStep(firstResult);
            // ... and after a test step has been completed ...
            uniqueTestNameAfterTestStepFinished = formatter.getUniqueTestName(testCase);
            formatter.finishTestCase();
            // ... and finally after the test case has been finished.
            uniqueTestNameAfterTestCaseFinished = formatter.getUniqueTestName(testCase);
        }

        // then
        private void assertExpectedUniqueTestName(String expectedUniqueTestName) {
            assertThat("Unexpected test name on test case initialization", uniqueTestNameOnTestCaseInitialization, is(nullValue()));
            assertThat("Unexpected test name after test case started", uniqueTestNameAfterTestCaseStarted, equalTo(expectedUniqueTestName));
            assertThat("Unexpected test name after test step finished", uniqueTestNameAfterTestStepFinished, equalTo(expectedUniqueTestName));
            assertThat("Unexpected test name after test case finished", uniqueTestNameAfterTestCaseFinished, equalTo(expectedUniqueTestName));
        }
    }

    private void mockResultStatus(Result result, Result.Type status) {
        when(result.getStatus()).thenReturn(status);
        when(result.is(Result.Type.PASSED)).thenReturn(status == Result.Type.PASSED);
    }
}
