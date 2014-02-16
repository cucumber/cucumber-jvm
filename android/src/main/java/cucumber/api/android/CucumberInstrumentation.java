package cucumber.api.android;

import android.app.Instrumentation;
import android.os.Bundle;

public class CucumberInstrumentation extends Instrumentation {
    public static final String REPORT_VALUE_ID = CucumberInstrumentationCore.REPORT_VALUE_ID;
    public static final String REPORT_KEY_NUM_TOTAL = CucumberInstrumentationCore.REPORT_KEY_NUM_TOTAL;
    public static final String TAG = CucumberInstrumentationCore.TAG;
    private CucumberInstrumentationCore instrumentationCore = new CucumberInstrumentationCore(this);

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        instrumentationCore.onCreate(arguments);
        start();
    }

    @Override
    public void onStart() {
        instrumentationCore.onStart();
    }

}
