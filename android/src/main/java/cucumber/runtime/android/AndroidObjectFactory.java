package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import cucumber.runtime.java.ObjectFactory;

/**
 * Android specific implementation of {@link cucumber.runtime.java.ObjectFactory} which will
 * make sure that created test classes have all necessary references to the executing {@link android.app.Instrumentation}
 * and the associated {@link android.content.Context}.
 */
public class AndroidObjectFactory implements ObjectFactory {

    /**
     * The actual {@link cucumber.runtime.java.ObjectFactory} responsible for creating instances.
     */
    private final ObjectFactory delegate;

    /**
     * The instrumentation to set to the objects.
     */
    private final Instrumentation instrumentation;

    /**
     * Creates a new instance using the given delegate {@link cucumber.runtime.java.ObjectFactory} to
     * forward all calls to and using the given {@link android.app.Instrumentation} to set to the instantiated
     * android test classes.
     *
     * @param delegate the {@link cucumber.runtime.java.ObjectFactory} to delegate to
     * @param instrumentation the {@link android.app.Instrumentation} to set to the tests
     */
    public AndroidObjectFactory(final ObjectFactory delegate, final Instrumentation instrumentation) {
        this.delegate = delegate;
        this.instrumentation = instrumentation;
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public void addClass(final Class<?> clazz) {
        delegate.addClass(clazz);
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        T instance = delegate.getInstance(type);
        decorate(instance);
        return instance;
    }

    private void decorate(final Object instance) {
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
