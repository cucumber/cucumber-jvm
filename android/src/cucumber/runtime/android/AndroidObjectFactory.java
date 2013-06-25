package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AndroidObjectFactory implements ObjectFactory {
    private final Instrumentation mInstrumentation;
    private final Set<Class<?>> mClasses = new HashSet<Class<?>>();
    private final Map<Class<?>, Object> mInstances = new HashMap<Class<?>, Object>();

    public AndroidObjectFactory(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }

    public void start() {
        // No-op
    }

    public void stop() {
        mInstances.clear();
    }

    public void addClass(Class<?> clazz) {
        mClasses.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        if (mInstances.containsKey(type)) {
            return type.cast(mInstances.get(type));
        } else {
            return cacheNewInstance(type);
        }
    }

    private <T> T cacheNewInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            T instance = constructor.newInstance();

            if (instance instanceof ActivityInstrumentationTestCase2) {
                ((ActivityInstrumentationTestCase2) instance).injectInstrumentation(mInstrumentation);
                // This Intent prevents the ActivityInstrumentationTestCase2 to stall on
                // Intent.startActivitySync (when calling getActivity) if the activity is already running.
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ((ActivityInstrumentationTestCase2) instance).setActivityIntent(intent);
            } else if (instance instanceof InstrumentationTestCase) {
                ((InstrumentationTestCase) instance).injectInstrumentation(mInstrumentation);
            } else if (instance instanceof AndroidTestCase) {
                ((AndroidTestCase) instance).setContext(mInstrumentation.getTargetContext());
            }
            mInstances.put(type, instance);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new CucumberException(String.format("%s doesn't have an empty constructor. If you need DI, put cucumber-picocontainer on the classpath", type), e);
        } catch (Exception e) {
            throw new CucumberException(String.format("Failed to instantiate %s", type), e);
        }
    }
}
