package cucumber.runtime.android;

import android.app.Instrumentation;
import android.os.Bundle;
import cucumber.runtime.Runtime;
import edu.emory.mathcs.backport.java.util.Collections;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static cucumber.runtime.android.AndroidInstrumentationReporter.StatusCodes;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AndroidInstrumentationReporterTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final Runtime runtime = mock(Runtime.class);
    private final Instrumentation instrumentation = mock(Instrumentation.class);

    private final Feature feature = mock(Feature.class);
    private final Scenario scenario = mock(Scenario.class);
    private final Match match = mock(Match.class);
    private final Result firstResult = mock(Result.class);
    private final Result secondResult = mock(Result.class);


    @Before
    public void beforeEachTest() {
        when(feature.getKeyword()).thenReturn("Feature");
        when(feature.getName()).thenReturn("Some important feature");
        when(scenario.getKeyword()).thenReturn("Scenario");
        when(scenario.getName()).thenReturn("Some important scenario");
    }

    @Test
    public void feature_name_and_keyword_is_contained_in_start_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);

        // when
        formatter.feature(feature);
        formatter.startOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.START), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString(feature.getKeyword()));
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString(feature.getName()));
    }

    @Test
    public void feature_name_and_keyword_is_contained_in_end_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.PASSED);

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.OK), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString(feature.getKeyword()));
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString(feature.getName()));
    }

    @Test
    public void scenario_name_and_keyword_is_contained_in_start_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);

        // when
        formatter.feature(feature);
        formatter.startOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.START), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(scenario.getKeyword()));
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(scenario.getName()));
    }

    @Test
    public void scenario_name_and_keyword_is_contained_in_end_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.PASSED);

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.OK), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(scenario.getKeyword()));
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(scenario.getName()));
    }

    @Test
    public void any_before_hook_exception_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("some random runtime exception"));

        // when
        formatter.feature(feature);
        formatter.before(match, firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some random runtime exception"));
    }

    @Test
    public void any_step_exception_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("some random runtime exception"));

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some random runtime exception"));

    }

    @Test
    public void any_after_hook_exception_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("some random runtime exception"));

        // when
        formatter.feature(feature);
        formatter.after(match, firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some random runtime exception"));
    }

    @Test
    public void any_failing_step_causes_test_failure() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new AssertionError("some test assertion went wrong"));
        when(firstResult.getErrorMessage()).thenReturn("some test assertion went wrong");

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some test assertion went wrong"));
    }

    @Test
    public void any_undefined_step_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.UNDEFINED.getStatus());
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void passing_step_causes_test_success() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 1);
        when(firstResult.getStatus()).thenReturn(Result.PASSED);

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        verify(instrumentation).sendStatus(eq(StatusCodes.OK), any(Bundle.class));
    }

    @Test
    public void skipped_step_causes_test_success() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.PASSED);
        when(secondResult.getStatus()).thenReturn(Result.SKIPPED.getStatus());

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        verify(instrumentation).sendStatus(eq(StatusCodes.OK), any(Bundle.class));

    }

    @Test
    public void first_before_exception_is_reported() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("first exception"));

        when(secondResult.getStatus()).thenReturn(Result.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("second exception"));

        // when
        formatter.feature(feature);
        formatter.before(match, firstResult);
        formatter.before(match, secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("first exception"));
    }

    @Test
    public void first_step_result_exception_is_reported() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("first exception"));

        when(secondResult.getStatus()).thenReturn(Result.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("second exception"));

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("first exception"));
    }

    @Test
    public void first_after_exception_is_reported() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new RuntimeException("first exception"));

        when(secondResult.getStatus()).thenReturn(Result.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("second exception"));

        // when
        formatter.feature(feature);
        formatter.after(match, firstResult);
        formatter.after(match, secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("first exception"));
    }

    @Test
    public void undefined_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.PASSED);

        when(secondResult.getStatus()).thenReturn(Result.UNDEFINED.getStatus());
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void failed_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.PASSED);

        when(secondResult.getStatus()).thenReturn(Result.FAILED);
        when(secondResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(secondResult.getErrorMessage()).thenReturn("some assertion went wrong");

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some assertion went wrong"));
    }

    @Test
    public void error_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.PASSED);

        when(secondResult.getStatus()).thenReturn(Result.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("some exception"));

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some exception"));
    }

    @Test
    public void failed_step_does_not_overrides_preceding_undefined_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.UNDEFINED.getStatus());
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        when(secondResult.getStatus()).thenReturn(Result.FAILED);
        when(secondResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(secondResult.getErrorMessage()).thenReturn("some assertion went wrong");

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void error_step_does_not_override_preceding_failed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(firstResult.getErrorMessage()).thenReturn("some assertion went wrong");

        when(secondResult.getStatus()).thenReturn(Result.FAILED);
        when(secondResult.getError()).thenReturn(new RuntimeException("some exception"));

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some assertion went wrong"));
    }

    @Test
    public void unexpected_status_code_causes_IllegalStateException() {
        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn("foobar");

        // then
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(containsString("foobar"));

        // when
        formatter.feature(feature);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);
    }

    @Test
    public void step_result_contains_only_the_current_scenarios_severest_result() {
        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation, 2);
        when(firstResult.getStatus()).thenReturn(Result.FAILED);
        when(firstResult.getError()).thenReturn(new AssertionError("some assertion went wrong"));
        when(firstResult.getErrorMessage()).thenReturn("some assertion went wrong");

        when(secondResult.getStatus()).thenReturn(Result.PASSED);

        // when
        formatter.feature(feature);
        formatter.startOfScenarioLifeCycle(scenario);
        formatter.result(firstResult);
        formatter.endOfScenarioLifeCycle(scenario);

        formatter.startOfScenarioLifeCycle(scenario);
        formatter.result(secondResult);
        formatter.endOfScenarioLifeCycle(scenario);

        // then

        final InOrder inOrder = inOrder(instrumentation);
        final ArgumentCaptor<Bundle> firstCaptor = ArgumentCaptor.forClass(Bundle.class);
        final ArgumentCaptor<Bundle> secondCaptor = ArgumentCaptor.forClass(Bundle.class);

        inOrder.verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), firstCaptor.capture());
        inOrder.verify(instrumentation).sendStatus(eq(StatusCodes.OK), secondCaptor.capture());
    }
}
