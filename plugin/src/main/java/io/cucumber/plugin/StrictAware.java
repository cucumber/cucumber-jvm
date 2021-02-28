package io.cucumber.plugin;

import org.apiguardian.api.API;

/**
 * Interface for Plugins that need to know if the Runtime is strict.
 *
 * @deprecated strict mode is enabled by default and will be removed.
 */
@Deprecated
@API(status = API.Status.STABLE)
public interface StrictAware extends Plugin {

    /**
     * When set to strict the plugin should indicate failure for undefined and
     * pending steps
     *
     * @param strict true if the runtime is in strict mode
     */
    void setStrict(boolean strict);

}
