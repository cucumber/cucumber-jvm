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
     * Verifies, if different test names are produced, if we uses scenario outlines with multiple examples.
     */
    @Test
    public void scenario_outline_all_test_names_unique() {
        // We really require this class in this method only.
        class TestNameTestSetup {
            private AndroidInstrumentationReporter formatter;
            private TestCase testCase;
            private String testCaseInitialized;
            private String testCaseStarted;
            private String testStepFinished;
            private String testCaseFinished;

            private TestNameTestSetup(AndroidInstrumentationReporter formatter, String featureName, String scenarioName) {
                this.formatter = formatter;
                this.testCase = mock(TestCase.class);
                when(testCase.getUri()).thenReturn("path/" + featureName + ".feature");
                when(testCase.getName()).thenReturn(scenarioName);
            }

            private TestNameTestSetup invokeGetUniqueTestNameOnDifferentStatesInTestCaseLifecycle() {
                // We invoke our method under test ...
                testCaseInitialized = formatter.getUniqueTestName(testCase);
                formatter.startTestCase(testCase);
                // ... we invoke the method again after test-case has been started ...
                testCaseStarted = formatter.getUniqueTestName(testCase);
                formatter.finishTestStep(firstResult);
                // ... and after a test step has been completed ...
                testStepFinished = formatter.getUniqueTestName(testCase);
                formatter.finishTestCase();
                // ... and finally after the test case has been finished.
                testCaseFinished = formatter.getUniqueTestName(testCase);
                return this;
            }

            private void assertValues(String expectedTestNameTestCaseStarted) {
                assertThat("Unexpected test name on test case initialization", testCaseInitialized, is(nullValue()));
                assertThat("Unexpected test name after test case start", testCaseStarted, equalTo(expectedTestNameTestCaseStarted));
                assertThat("Unexpected test name after test step finished", testStepFinished, equalTo(expectedTestNameTestCaseStarted));
                assertThat("Unexpected test name after test case finished",testCaseFinished, equalTo(expectedTestNameTestCaseStarted));
            }
        }
        String[] featureNames = new String[]{"Addition", "Subtraction", "Multiplication", "Division"};
        String[] scenarioNames = new String[] {"Enter one number", "Enter two numbers"};
        List<TestNameTestSetup> testNameTestSetupCollection = new ArrayList<TestNameTestSetup>();
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        mockResultStatus(firstResult, Result.Type.PASSED);
        final int MAX_EXAMPLES = 3;
        // Normally, it is not good style to use for-loops in tests, but in this case it should be okay.
        for (String featureName : featureNames) {
            for (String scenarioName : scenarioNames) {
                for (int exampleIndex = 0; exampleIndex < MAX_EXAMPLES; exampleIndex++) {
                    TestNameTestSetup testNameTestSetup = new TestNameTestSetup(formatter, featureName, scenarioName);
                    testNameTestSetup.invokeGetUniqueTestNameOnDifferentStatesInTestCaseLifecycle();
                    testNameTestSetupCollection.add(testNameTestSetup);
                }
            }
        }

        testNameTestSetupCollection.add(new TestNameTestSetup(formatter, featureNames[0], scenarioNames[0])
            .invokeGetUniqueTestNameOnDifferentStatesInTestCaseLifecycle()
        );
        testNameTestSetupCollection.add(new TestNameTestSetup(formatter, "New feature", scenarioNames[0])
            .invokeGetUniqueTestNameOnDifferentStatesInTestCaseLifecycle()
        );
        testNameTestSetupCollection.add(new TestNameTestSetup(formatter, featureNames[0], "new_scenario")
            .invokeGetUniqueTestNameOnDifferentStatesInTestCaseLifecycle()
        );
        testNameTestSetupCollection.add(new TestNameTestSetup(formatter, featureNames[0], "new_scenario")
            .invokeGetUniqueTestNameOnDifferentStatesInTestCaseLifecycle()
        );

        // TODO Consider to split this test method into multiple ones, to fulfil the one-assert-per-test-method-paradigm
        testNameTestSetupCollection.get(0).assertValues("Enter one number");
        testNameTestSetupCollection.get(1).assertValues("Enter one number 2");
        testNameTestSetupCollection.get(2).assertValues("Enter one number 3");
        testNameTestSetupCollection.get(3).assertValues("Enter two numbers");
        testNameTestSetupCollection.get(4).assertValues("Enter two numbers 2");
        testNameTestSetupCollection.get(5).assertValues("Enter two numbers 3");

        testNameTestSetupCollection.get(6).assertValues("Enter one number");
        testNameTestSetupCollection.get(7).assertValues("Enter one number 2");
        testNameTestSetupCollection.get(8).assertValues("Enter one number 3");
        testNameTestSetupCollection.get(9).assertValues("Enter two numbers");
        testNameTestSetupCollection.get(10).assertValues("Enter two numbers 2");
        testNameTestSetupCollection.get(11).assertValues("Enter two numbers 3");

        testNameTestSetupCollection.get(12).assertValues("Enter one number");
        testNameTestSetupCollection.get(13).assertValues("Enter one number 2");
        testNameTestSetupCollection.get(14).assertValues("Enter one number 3");
        testNameTestSetupCollection.get(15).assertValues("Enter two numbers");
        testNameTestSetupCollection.get(16).assertValues("Enter two numbers 2");
        testNameTestSetupCollection.get(17).assertValues("Enter two numbers 3");

        testNameTestSetupCollection.get(18).assertValues("Enter one number");
        testNameTestSetupCollection.get(19).assertValues("Enter one number 2");
        testNameTestSetupCollection.get(20).assertValues("Enter one number 3");
        testNameTestSetupCollection.get(21).assertValues("Enter two numbers");
        testNameTestSetupCollection.get(22).assertValues("Enter two numbers 2");
        testNameTestSetupCollection.get(23).assertValues("Enter two numbers 3");

        testNameTestSetupCollection.get(24).assertValues("Enter one number 4");
        testNameTestSetupCollection.get(25).assertValues("Enter one number");
        testNameTestSetupCollection.get(26).assertValues("new_scenario");
        testNameTestSetupCollection.get(27).assertValues("new_scenario_2");

        assertThat(testNameTestSetupCollection.size(), equalTo(28));
    }

    private void mockResultStatus(Result result, Result.Type status) {
        when(result.getStatus()).thenReturn(status);
        when(result.is(Result.Type.PASSED)).thenReturn(status == Result.Type.PASSED);
    }
}
