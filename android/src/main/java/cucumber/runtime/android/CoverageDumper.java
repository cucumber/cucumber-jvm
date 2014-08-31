package cucumber.runtime.android;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Dumps coverage data into a file.
 */
public class CoverageDumper {

    /**
     * The key for the result bundle value which will contain the path to file containing the coverage data.
     */
    private static final String RESULT_KEY_COVERAGE_PATH = "coverageFilePath";

    /**
     * The string format to be appended to the result stream in case coverage data could be dumped successfully.
     */
    private static final String RESULT_STREAM_SUCCESS_OUTPUT_FORMAT = "Generated code coverage data to %s";

    /**
     * The string to be logged with logcat in case coverage data could not be dumped successfully.
     */
    private static final String LOG_ERROR_OUTPUT = "Failed to generate coverage.";

    /**
     * The string to be appended to the result stream in case coverage data could not be dumped successfully.
     */
    private static final String RESULT_STREAM_ERROR_OUTPUT = "Error: Failed to generate coverage. Check logcat for details.";

    /**
     * The implementation of the code coverage tool.
     * Currently known implementations are emma and jacoco.
     */
    private static final String IMPLEMENTATION_CLASS = "com.vladium.emma.rt.RT";

    /**
     * The method to call for dumping the coverage data.
     */
    private static final String IMPLEMENTATION_METHOD = "dumpCoverageData";

    /**
     * The arguments to work with.
     */
    private final Arguments arguments;

    /**
     * Creates a new instance for the given arguments.
     *
     * @param arguments the arguments to work with
     */
    public CoverageDumper(final Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Dumps the coverage data into the given file, if code coverage is enabled.
     *
     * @param bundle the {@link Bundle} to put coverage information into
     */
    public void requestDump(final Bundle bundle) {

        if (!arguments.isCoverageEnabled()) {
            return;
        }

        final String coverageDateFilePath = arguments.coverageDataFilePath();
        final File coverageFile = new File(coverageDateFilePath);

        try {
            final Class dumperClass = Class.forName(IMPLEMENTATION_CLASS);
            final Method dumperMethod = dumperClass.getMethod(IMPLEMENTATION_METHOD, coverageFile.getClass(), boolean.class, boolean.class);
            dumperMethod.invoke(null, coverageFile, false, false);

            bundle.putString(RESULT_KEY_COVERAGE_PATH, coverageDateFilePath);
            appendNewLineToResultStream(bundle, String.format(RESULT_STREAM_SUCCESS_OUTPUT_FORMAT, coverageDateFilePath));
        } catch (final ClassNotFoundException e) {
            reportError(bundle, e);
        } catch (final SecurityException e) {
            reportError(bundle, e);
        } catch (final NoSuchMethodException e) {
            reportError(bundle, e);
        } catch (final IllegalAccessException e) {
            reportError(bundle, e);
        } catch (final InvocationTargetException e) {
            reportError(bundle, e);
        }
    }

    private void reportError(final Bundle results, final Exception e) {
        Log.e(CucumberExecutor.TAG, LOG_ERROR_OUTPUT, e);
        appendNewLineToResultStream(results, RESULT_STREAM_ERROR_OUTPUT);
    }

    private void appendNewLineToResultStream(final Bundle results, final String message) {
        final String currentStream = results.getString(Instrumentation.REPORT_KEY_STREAMRESULT);
        results.putString(Instrumentation.REPORT_KEY_STREAMRESULT, currentStream + "\n" + message);
    }
}
