package cucumber.runtime.android;

import android.os.Bundle;

/**
 * Holds instrumentation arguments.
 */
public class Arguments {

    public static final String VALUE_SEPARATOR = "--";

    /**
     * Keys of supported arguments.
     */
    public static class KEY {
        public static final String COUNT_ENABLED = "count";
        public static final String DEBUG_ENABLED = "debug";
        public static final String COVERAGE_ENABLED = "coverage";
        public static final String COVERAGE_DATA_FILE_PATH = "coverageFile";
    }

    /**
     * Default values of supported arguments.
     */
    public static class DEFAULT {
        public static final String COVERAGE_DATA_FILE_PATH = "coverage.ec";
    }

    private final boolean countEnabled;
    private final boolean debugEnabled;
    private final boolean coverageEnabled;
    private final String coverageDataFilePath;
    private final String cucumberOptions;

    /**
     * Constructs a new instance with arguments extracted from the given {@code bundle}.
     *
     * @param bundle the {@link Bundle} to extract the arguments from
     */
    public Arguments(final Bundle bundle) {
        countEnabled = getBooleanArgument(bundle, KEY.COUNT_ENABLED);
        debugEnabled = getBooleanArgument(bundle, KEY.DEBUG_ENABLED);
        coverageEnabled = getBooleanArgument(bundle, KEY.COVERAGE_ENABLED);
        coverageDataFilePath = getStringArgument(bundle, KEY.COVERAGE_DATA_FILE_PATH, DEFAULT.COVERAGE_DATA_FILE_PATH);
        cucumberOptions = getCucumberOptionsString(bundle);
    }

    /**
     * @return whether tests should not be executed, but just being counted
     */
    public boolean isCountEnabled() {
        return countEnabled;
    }

    /**
     * @return whether debugging is enabled or not
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * @return the path to the coverage data file, defaults to "coverage.ec"
     */
    public String coverageDataFilePath() {
        return coverageDataFilePath;
    }

    /**
     * @return whether coverage is enabled or not
     */
    public boolean isCoverageEnabled() {
        return coverageEnabled;
    }

    /**
     * @return the cucumber options string
     */
    public String getCucumberOptions() {
        return cucumberOptions;
    }

    /**
     * Extracts a boolean value from the bundle which is stored as string.
     * Given the string value is "true" the boolean value will be {@code true},
     * given the string value is "false the boolean value will be {@code false}.
     * The case in the string is ignored. In case no value is found this method
     * returns false. In case the given {@code bundle} is {@code null} {@code false}
     * will be returned.
     *
     * @param bundle the {@link Bundle} to get the value from
     * @param key    the key to get the value for
     * @return the boolean representation of the string value found for the given key,
     * or false in case no value was found
     */
    private boolean getBooleanArgument(final Bundle bundle, final String key) {

        if (bundle == null) {
            return false;
        }

        final String tagString = bundle.getString(key);
        return tagString != null && Boolean.parseBoolean(tagString);
    }

    /**
     * Extracts a string value from the bundle, gracefully falling back to the provided {@code defaultValue}
     * in case no value could be found for the given {@code key} or the {@code bundle} was {@code null}.
     *
     * @param bundle       the {@link Bundle} to get the value from
     * @param key          the key to get the value for
     * @param defaultValue the default value to take in case no value could be found or the {@code bundle} was {@code null}
     * @return the string value for the given {@code key}
     */
    private String getStringArgument(final Bundle bundle, final String key, final String defaultValue) {
        if (bundle == null) {
            return defaultValue;
        }
        return bundle.getString(key, defaultValue);
    }

    /**
     * Adds the given {@code optionKey} and its {@code optionValue} tot he given string buffer. This method will split
     * potential multiple option values separated by {@link cucumber.runtime.android.Arguments#VALUE_SEPARATOR} into a space
     * separated list of those values.
     */
    private void appendOption(final StringBuilder sb, final String optionKey, final String optionValue) {
        for (final String value : optionValue.split(VALUE_SEPARATOR)) {
            sb.append(sb.length() == 0 || optionKey.isEmpty() ? "" : " ").append(optionKey).append(optionValue.isEmpty() ? "" : " " + value);
        }
    }

    /**
     * Returns a Cucumber options compatible string based on the argument extras found.
     * <p/>
     * The bundle <em>cannot</em> contain multiple entries for the same key,
     * however certain Cucumber options can be passed multiple times (e.g.
     * {@code --tags}). The solution is to pass values separated by
     * {@link Arguments#VALUE_SEPARATOR} which will result
     * in multiple {@code --key value} pairs being created.
     *
     * @param bundle the {@link Bundle} to get the values from
     * @return the cucumber options string
     */
    private String getCucumberOptionsString(final Bundle bundle) {

        if (bundle == null) {
            return "";
        }

        final String cucumberOptions = bundle.getString("cucumberOptions");
        if (cucumberOptions != null) {
            return cucumberOptions;
        }

        final StringBuilder sb = new StringBuilder();
        String features = "";

        for (final String key : bundle.keySet()) {
            if ("glue".equals(key)) {
                appendOption(sb, "--glue", bundle.getString(key));
            } else if ("format".equals(key)) {
                appendOption(sb, "--format", bundle.getString(key));
            } else if ("plugin".equals(key)) {
                appendOption(sb, "--plugin", bundle.getString(key));
            } else if ("tags".equals(key)) {
                appendOption(sb, "--tags", bundle.getString(key));
            } else if ("name".equals(key)) {
                appendOption(sb, "--name", bundle.getString(key));
            } else if ("dryRun".equals(key) && getBooleanArgument(bundle, key)) {
                appendOption(sb, "--dry-run", "");
            } else if ("log".equals(key) && getBooleanArgument(bundle, key)) {
                appendOption(sb, "--dry-run", "");
            } else if ("noDryRun".equals(key) && getBooleanArgument(bundle, key)) {
                appendOption(sb, "--no-dry-run", "");
            } else if ("monochrome".equals(key) && getBooleanArgument(bundle, key)) {
                appendOption(sb, "--monochrome", "");
            } else if ("noMonochrome".equals(key) && getBooleanArgument(bundle, key)) {
                appendOption(sb, "--no-monochrome", "");
            } else if ("strict".equals(key) && getBooleanArgument(bundle, key)) {
                appendOption(sb, "--strict", "");
            } else if ("noStrict".equals(key) && getBooleanArgument(bundle, key)) {
                appendOption(sb, "--no-strict", "");
            } else if ("snippets".equals(key)) {
                appendOption(sb, "--snippets", bundle.getString(key));
            } else if ("features".equals(key)) {
                features = bundle.getString(key);
            }
        }
        // Even though not strictly required, wait until everything else
        // has been added before adding any feature references
        appendOption(sb, "", features);
        return sb.toString();
    }
}
