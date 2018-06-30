package cucumber.api.formatter;

import cucumber.api.Plugin;

/**
 * Interface for Plugins that use ANSI escape codes to print coloured output.
 */
public interface ColorAware extends Plugin {
    /**
     * When set to monochrome the plugin should not use colored output.
     * <p>
     * For the benefit of systems that do not support ANSI escape codes.
     *
     * @param monochrome true if monochrome output should be used
     */
    void setMonochrome(boolean monochrome);
}
