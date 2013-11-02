package cucumber.runtime.android;

import android.os.Bundle;

/**
 * This is a wrapper class around the command line arguments that were supplied
 * when the instrumentation was started.
 */
public final class InstrumentationArguments {
    private static final String KEY_DEBUG = "debug";
    private static final String KEY_LOG = "log";
    private static final String KEY_COUNT = "count";
    private static final String KEY_COVERAGE = "coverage";
    private static final String KEY_COVERAGE_FILE_PATH = "coverageFile";
    private static final String VALUE_SEPARATOR = "--";

    private Bundle arguments;

    public InstrumentationArguments(Bundle arguments) {
        this.arguments = arguments != null ? arguments : new Bundle();
    }

    private boolean getBooleanArgument(String tag) {
        String tagString = arguments.getString(tag);
        return tagString != null && Boolean.parseBoolean(tagString);
    }

    private void appendOption(StringBuilder sb, String optionKey, String optionValue) {
        for (String value : optionValue.split(VALUE_SEPARATOR)) {
            sb.append(sb.length() == 0 || optionKey.isEmpty() ? "" : " ").append(optionKey).append(optionValue.isEmpty() ? "" : " " + value);
        }
    }

    /**
     * Returns a Cucumber options compatible string based on the argument extras found.
     * <p />
     * The bundle <em>cannot</em> contain multiple entries for the same key,
     * however certain Cucumber options can be passed multiple times (e.g.
     * {@code --tags}). The solution is to pass values separated by
     * {@link InstrumentationArguments#VALUE_SEPARATOR} which will result
     * in multiple {@code --key value} pairs being created.
     *
     * @return the cucumber options string
     */
    public String getCucumberOptionsString() {
        String cucumberOptions = arguments.getString("cucumberOptions");
        if (cucumberOptions != null) {
            return cucumberOptions;
        }

        StringBuilder sb = new StringBuilder();
        String features = "";
        for (String key : arguments.keySet()) {
            if ("glue".equals(key)) {
                appendOption(sb, "--glue", arguments.getString(key));
            } else if ("format".equals(key)) {
                appendOption(sb, "--format", arguments.getString(key));
            } else if ("tags".equals(key)) {
                appendOption(sb, "--tags", arguments.getString(key));
            } else if ("name".equals(key)) {
                appendOption(sb, "--name", arguments.getString(key));
            } else if ("dryRun".equals(key) && getBooleanArgument(key)) {
                appendOption(sb, "--dry-run", "");
            } else if ("log".equals(key) && getBooleanArgument(key)) {
                appendOption(sb, "--dry-run", "");
            } else if ("noDryRun".equals(key) && getBooleanArgument(key)) {
                appendOption(sb, "--no-dry-run", "");
            } else if ("monochrome".equals(key) && getBooleanArgument(key)) {
                appendOption(sb, "--monochrome", "");
            } else if ("noMonochrome".equals(key) && getBooleanArgument(key)) {
                appendOption(sb, "--no-monochrome", "");
            } else if ("strict".equals(key) && getBooleanArgument(key)) {
                appendOption(sb, "--strict", "");
            } else if ("noStrict".equals(key) && getBooleanArgument(key)) {
                appendOption(sb, "--no-strict", "");
            } else if ("snippets".equals(key)) {
                appendOption(sb, "--snippets", arguments.getString(key));
            } else if ("dotcucumber".equals(key)) {
                appendOption(sb, "--dotcucumber", arguments.getString(key));
            } else if ("features".equals(key)) {
                features = arguments.getString(key);
            }
        }
        // Even though not strictly required, wait until everything else
        // has been added before adding any feature references
        appendOption(sb, "", features);
        return sb.toString();
    }

    public boolean isDebugEnabled() {
        return getBooleanArgument(KEY_DEBUG);
    }

    public boolean isLogEnabled() {
        return getBooleanArgument(KEY_LOG);
    }

    public boolean isCountEnabled() {
        return getBooleanArgument(KEY_COUNT);
    }

    public boolean isCoverageEnabled() {
        return getBooleanArgument(KEY_COVERAGE);
    }

    public String getCoverageFilePath() {
        return arguments.getString(KEY_COVERAGE_FILE_PATH);
    }
}
