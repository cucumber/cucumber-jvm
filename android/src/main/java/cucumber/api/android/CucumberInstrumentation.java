package cucumber.api.android;

import android.app.Instrumentation;
import android.os.Bundle;

/**
 * A simple extension of the {@link android.app.Instrumentation} utilizing the {@link cucumber.api.android.CucumberInstrumentationCore}.
 */
public class CucumberInstrumentation extends Instrumentation {

    /**
     * The {@link cucumber.api.android.CucumberInstrumentationCore} which will run the actual logic using this {@link android.app.Instrumentation}
     * implementation.
     */
    private CucumberInstrumentationCore cucumberInstrumentationCore = new CucumberInstrumentationCore(this);

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        cucumberInstrumentationCore.create(bundle);
        start();
    }

    @Override
    public void onStart() {
        cucumberInstrumentationCore.start();
    }

}
