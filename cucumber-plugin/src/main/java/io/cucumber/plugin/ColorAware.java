package io.cucumber.plugin;

import org.apiguardian.api.API;

/**
 * Interface for Plugins that use ANSI escape codes to print coloured output.
 */
@API(status = API.Status.STABLE)
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
