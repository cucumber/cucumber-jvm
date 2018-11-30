package cucumber.api.formatter;

import cucumber.api.Plugin;

/**
 * Interface for Plugins that need to know if the Runtime is strict.
 */
public interface StrictAware extends Plugin {
    /**
     * When set to strict the plugin should indicate failure for undefined and pending steps
     *
     * @param strict true if the runtime is in strict mode
     */
    void setStrict(boolean strict);
}
