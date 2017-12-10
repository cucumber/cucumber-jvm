package cucumber.api.formatter;

/**
 * Interface for Formatters that need to know if the Runtime is strict.
 */
public interface StrictAware extends Formatter {
    /**
     * When set to strict the formatter should indicate failure for undefined and pending steps
     *
     * @param strict true iff the runtime is in strict mode
     */
    void setStrict(boolean strict);
}
