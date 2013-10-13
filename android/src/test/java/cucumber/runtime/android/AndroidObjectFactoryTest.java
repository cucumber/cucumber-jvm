package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 16, manifest = Config.NONE)
public class AndroidObjectFactoryTest {

    private final ObjectFactory delegate = mock(ObjectFactory.class);
    private final Instrumentation instrumentation = mock(Instrumentation.class);
    private final AndroidObjectFactory androidObjectFactory = new AndroidObjectFactory(delegate, instrumentation);

    @Test
    public void delegates_start_call() {

        // when
        androidObjectFactory.start();

        // then
        verify(delegate).start();
    }

    @Test
    public void delegates_stop_call() {

        // when
        androidObjectFactory.stop();

        // then
        verify(delegate).stop();
    }

    @Test
    public void delegates_addClass_call() {

        // given
        final Class<?> someClass = String.class;

        // when
        androidObjectFactory.addClass(someClass);

        // then
        verify(delegate).addClass(String.class);
    }

    @Test
    public void delegates_getInstance_call() {

        // given
        final Class<?> someClass = String.class;

        // when
        androidObjectFactory.getInstance(someClass);

        // then
        verify(delegate).getInstance(someClass);

    }

    @Test
    public void injects_instrumentation_into_ActivityInstrumentationTestCase2() {

        // given
        final Class<?> activityInstrumentationTestCase2Class = ActivityInstrumentationTestCase2.class;
        final ActivityInstrumentationTestCase2 activityInstrumentationTestCase2 = mock(ActivityInstrumentationTestCase2.class);
        when(delegate.getInstance(activityInstrumentationTestCase2Class)).thenReturn(activityInstrumentationTestCase2);

        // when
        androidObjectFactory.getInstance(activityInstrumentationTestCase2Class);

        // then
        verify(activityInstrumentationTestCase2).injectInstrumentation(instrumentation);
    }

    @Test
    public void sets_activity_intent_with_FLAG_ACTIVITY_CLEAR_TOP_to_prevent_stalling_when_calling_getActivity_if_the_activity_is_already_running() {

        // given
        final Class<?> activityInstrumentationTestCase2Class = ActivityInstrumentationTestCase2.class;
        final ActivityInstrumentationTestCase2 activityInstrumentationTestCase2 = mock(ActivityInstrumentationTestCase2.class);
        when(delegate.getInstance(activityInstrumentationTestCase2Class)).thenReturn(activityInstrumentationTestCase2);
        final Intent intent = new Intent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // when
        androidObjectFactory.getInstance(activityInstrumentationTestCase2Class);

        // then
        verify(activityInstrumentationTestCase2).setActivityIntent(intent);
    }

    @Test
    public void injects_instrumentation_into_InstrumentationTestCase() {

        // given
        final Class<?> instrumentationTestCaseClass = InstrumentationTestCase.class;
        final InstrumentationTestCase instrumentationTestCase = mock(InstrumentationTestCase.class);
        when(delegate.getInstance(instrumentationTestCaseClass)).thenReturn(instrumentationTestCase);

        // when
        androidObjectFactory.getInstance(instrumentationTestCaseClass);

        // then
        verify(instrumentationTestCase).injectInstrumentation(instrumentation);
    }

    @Test
    public void injects_instrumentation_context_into_AndroidTestCase() {

        // given
        final Class<?> androidTestCaseClass = AndroidTestCase.class;
        final AndroidTestCase androidTestCase = mock(AndroidTestCase.class);
        when(delegate.getInstance(androidTestCaseClass)).thenReturn(androidTestCase);
        final Context context = mock(Context.class);
        when(instrumentation.getTargetContext()).thenReturn(context);

        // when
        androidObjectFactory.getInstance(androidTestCaseClass);

        // then
        verify(androidTestCase).setContext(context);

    }
}