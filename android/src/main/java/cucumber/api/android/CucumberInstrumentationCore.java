package cucumber.api.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Looper;
import cucumber.runtime.android.Arguments;
import cucumber.runtime.android.CoverageDumper;
import cucumber.runtime.android.CucumberExecutor;
import cucumber.runtime.android.Waiter;


public class CucumberInstrumentationCore {

    public static final String REPORT_VALUE_ID = CucumberInstrumentationCore.class.getSimpleName();
    public static final String REPORT_KEY_NUM_TOTAL = "numtests";

    private final Instrumentation instrumentation;
    private Waiter waiter;
    private CoverageDumper coverageDumper;
    private CucumberExecutor cucumberExecutor;
    private Arguments arguments;

    public CucumberInstrumentationCore(final Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public void create(final Bundle bundle) {
        arguments = new Arguments(bundle);
        cucumberExecutor = new CucumberExecutor(arguments, instrumentation);
        coverageDumper = new CoverageDumper(arguments);
        waiter = new Waiter(arguments);
    }

    public void start() {
        Looper.prepare();

        final Bundle results = new Bundle();
        if (arguments.isCountEnabled()) {
            results.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            results.putInt(REPORT_KEY_NUM_TOTAL, cucumberExecutor.getNumberOfTests());
        } else {
            waiter.requestWaitForDebugger();
            cucumberExecutor.execute();
            coverageDumper.requestDump(results);
        }

        instrumentation.finish(Activity.RESULT_OK, results);
    }
}
