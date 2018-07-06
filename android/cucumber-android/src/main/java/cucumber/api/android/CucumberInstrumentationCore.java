package cucumber.api.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Looper;
import cucumber.runtime.android.Arguments;
import cucumber.runtime.android.CoverageDumper;
import cucumber.runtime.android.CucumberExecutor;
import cucumber.runtime.android.DebuggerWaiter;


/**
 * The composition based instrumentation logic for running cucumber scenarios.
 */
public class CucumberInstrumentationCore {

    /**
     * The value to be used for the {@link Instrumentation#REPORT_KEY_IDENTIFIER}.
     */
    public static final String REPORT_VALUE_ID = CucumberInstrumentationCore.class.getSimpleName();

    /**
     * The report key for storing the number of to be executed scenarios.
     */
    public static final String REPORT_KEY_NUM_TOTAL = "numtests";

    /**
     * The {@link android.app.Instrumentation} to report results to.
     */
    private final Instrumentation instrumentation;

    /**
     * Used to wait for the debugger to be attached before actually running the scenarios.
     */
    private DebuggerWaiter debuggerWaiter;

    /**
     * Used to dump code coverage results at the end of the test execution
     */
    private CoverageDumper coverageDumper;

    /**
     * Responsible for the actual execution of the scenarios.
     */
    private CucumberExecutor cucumberExecutor;

    /**
     * Holds all cucumber relevant arguments passed to the {@link android.app.Instrumentation} inside the {@link Bundle}.
     */
    private Arguments arguments;

    /**
     * Creates a new instance for the given {@code instrumentation}.
     *
     * @param instrumentation the {@link android.app.Instrumentation} to use when running scenarios
     */
    public CucumberInstrumentationCore(final Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    /**
     * This method should be used to forward the {@link android.app.Instrumentation#onCreate(android.os.Bundle)} parameter
     * to this classes logic. It must be called before the call to {@link android.app.Instrumentation#start()}.
     *
     * @param bundle the bundle passed to the {@link android.app.Instrumentation#onCreate(android.os.Bundle)} method.
     */
    public void create(final Bundle bundle) {
        arguments = new Arguments(bundle);
        cucumberExecutor = new CucumberExecutor(arguments, instrumentation);
        coverageDumper = new CoverageDumper(arguments);
        debuggerWaiter = new DebuggerWaiter(arguments);
    }

    /**
     * This method should be used to forward the event of the start of the instrumentation, meaning it should be called in the
     * {@link android.app.Instrumentation#onStart()} method.
     */
    public void start() {
        Looper.prepare();

        final Bundle results = new Bundle();
        if (arguments.isCountEnabled()) {
            results.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            results.putInt(REPORT_KEY_NUM_TOTAL, cucumberExecutor.getNumberOfConcreteScenarios());
        } else {
            debuggerWaiter.requestWaitForDebugger();
            cucumberExecutor.execute();
            coverageDumper.requestDump(results);
        }

        instrumentation.finish(Activity.RESULT_OK, results);
    }
}
