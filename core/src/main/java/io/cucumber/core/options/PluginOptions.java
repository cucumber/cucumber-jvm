package io.cucumber.core.options;

import io.cucumber.core.plugin.Options;

public interface PluginOptions extends Options {

    boolean isStrict();

    boolean isMonochrome();

}
