package cucumber.api.android;

import android.app.Instrumentation;
import android.os.Bundle;

public class CucumberInstrumentation extends Instrumentation {
    public static final String REPORT_VALUE_ID = CucumberInstrumentationHelper.REPORT_VALUE_ID;
    public static final String REPORT_KEY_NUM_TOTAL = CucumberInstrumentationHelper.REPORT_KEY_NUM_TOTAL;
    public static final String TAG = CucumberInstrumentationHelper.TAG;
    private CucumberInstrumentationHelper helper = new CucumberInstrumentationHelper(this);

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        helper.onCreate(arguments);
        start();
    }

    @Override
    public void onStart() {
        helper.onStart();
    }

}
