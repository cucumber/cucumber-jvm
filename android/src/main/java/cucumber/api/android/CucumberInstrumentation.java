package cucumber.api.android;

import android.app.Instrumentation;
import android.os.Bundle;

public class CucumberInstrumentation extends Instrumentation {

    private CucumberInstrumentationCore instrumentationCore = new CucumberInstrumentationCore(this);

    @Override
    public void onCreate(final Bundle arguments) {
        super.onCreate(arguments);
        instrumentationCore.create(arguments);
        start();
    }

    @Override
    public void onStart() {
        instrumentationCore.start();
    }

}
