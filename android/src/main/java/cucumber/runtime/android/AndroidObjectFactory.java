package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import cucumber.runtime.java.ObjectFactory;

public class AndroidObjectFactory implements ObjectFactory {
    private final ObjectFactory delegate;
    private final Instrumentation instrumentation;

    public AndroidObjectFactory(ObjectFactory delegate, Instrumentation instrumentation) {
        this.delegate = delegate;
        this.instrumentation = instrumentation;
    }

    public void start() {
        delegate.start();
    }

    public void stop() {
        delegate.stop();
    }

    public void addClass(Class<?> clazz) {
        delegate.addClass(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        T instance = delegate.getInstance(type);
        decorate(instance);
        return instance;
    }

    private void decorate(Object instance) {
        if (instance instanceof ActivityInstrumentationTestCase2) {
            ((ActivityInstrumentationTestCase2) instance).injectInstrumentation(instrumentation);
            // This Intent prevents the ActivityInstrumentationTestCase2 to stall on
            // Intent.startActivitySync (when calling getActivity) if the activity is already running.
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((ActivityInstrumentationTestCase2) instance).setActivityIntent(intent);
        } else if (instance instanceof InstrumentationTestCase) {
            ((InstrumentationTestCase) instance).injectInstrumentation(instrumentation);
        } else if (instance instanceof AndroidTestCase) {
            ((AndroidTestCase) instance).setContext(instrumentation.getTargetContext());
        }
    }
}
