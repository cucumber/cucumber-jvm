package cucumber.api.formatter;

/**
 * Interface for Formatters that use ANSI escape codes to print coloured output.
 */
public interface ColorAware extends Formatter {
    /**
     * When set to monochrome the formatter should not use colored output.
     * <p>
     * For the benefit of systems that do not support ANSI escape codes.
     *
     * @param monochrome true iff monochrome output should be used
     */
    void setMonochrome(boolean monochrome);
}
