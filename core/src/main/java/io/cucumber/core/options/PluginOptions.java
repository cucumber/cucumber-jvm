package io.cucumber.core.options;

import java.util.List;

public interface PluginOptions {
    List<String> getPluginNames();

    boolean isStrict();

    boolean isMonochrome();

}
