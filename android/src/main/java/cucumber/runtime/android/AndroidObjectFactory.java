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

    public AndroidObjectFactory(final ObjectFactory delegate, final Instrumentation instrumentation) {
        this.delegate = delegate;
        this.instrumentation = instrumentation;
    }

    public void start() {
        delegate.start();
    }

    public void stop() {
        delegate.stop();
    }

    public void addClass(final Class<?> clazz) {
        delegate.addClass(clazz);
    }

    public <T> T getInstance(final Class<T> type) {
        T instance = delegate.getInstance(type);
        decorate(instance);
        return instance;
    }

    private void decorate(Object instance) {
        if (instance instanceof ActivityInstrumentationTestCase2) {
            final ActivityInstrumentationTestCase2 activityInstrumentationTestCase2 = (ActivityInstrumentationTestCase2) instance;
            activityInstrumentationTestCase2.injectInstrumentation(instrumentation);
            final Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activityInstrumentationTestCase2.setActivityIntent(intent);
        } else if (instance instanceof InstrumentationTestCase) {
            ((InstrumentationTestCase) instance).injectInstrumentation(instrumentation);
        } else if (instance instanceof AndroidTestCase) {
            ((AndroidTestCase) instance).setContext(instrumentation.getTargetContext());
        }
    }
}
