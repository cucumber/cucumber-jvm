package cucumber.runtime.android;

import android.util.Log;
import cucumber.runtime.Runtime;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import java.util.List;

/**
 * Logs information about the currently executed statements to androids logcat.
 */
public class  AndroidLogcatReporter extends NoOpFormattingReporter {

    /**
     * The {@link cucumber.runtime.Runtime} to get the errors and snippets from for writing them to the logcat at the end of the execution.
     */
    private final Runtime runtime;

    /**
     * The log tag to be used when logging to logcat.
     */
    private final String logTag;

    /**
     * Holds the feature's uri.
     */
    private String uri;

    /**
     * Creates a new instance for the given parameters.
     *
     * @param runtime the {@link cucumber.runtime.Runtime} to get the errors and snippets from
     * @param logTag the tag to use for logging to logcat
     */
    public AndroidLogcatReporter(final Runtime runtime, final String logTag) {
        this.runtime = runtime;
        this.logTag = logTag;
    }

    @Override
    public void uri(final String uri) {
        this.uri = uri;
    }

    @Override
    public void feature(final Feature feature) {
        Log.d(logTag, String.format("%s: %s (%s)%n%s", feature.getKeyword(), feature.getName(), uri, feature.getDescription()));
    }

    @Override
    public void background(final Background background) {
        Log.d(logTag, background.getName());
    }

    @Override
    public void scenario(final Scenario scenario) {
        Log.d(logTag, String.format("%s: %s", scenario.getKeyword(), scenario.getName()));
    }

    @Override
    public void scenarioOutline(final ScenarioOutline scenarioOutline) {
        Log.d(logTag, String.format("%s: %s", scenarioOutline.getKeyword(), scenarioOutline.getName()));
    }

    @Override
    public void examples(final Examples examples) {
        Log.d(logTag, String.format("%s: %s", examples.getKeyword(), examples.getName()));
    }

    @Override
    public void step(final Step step) {
        Log.d(logTag, String.format("%s%s", step.getKeyword(), step.getName()));
    }

    @Override
    public void syntaxError(final String state, final String event, final List<String> legalEvents, final String uri, final Integer line) {
        Log.e(logTag, String.format("syntax error '%s' %s:%d", event, uri, line));
    }

    @Override
    public void done() {
        for (final Throwable throwable : runtime.getErrors()) {
            Log.e(logTag, throwable.toString());
        }

        for (final String snippet : runtime.getSnippets()) {
            Log.w(logTag, snippet);
        }
    }
}
