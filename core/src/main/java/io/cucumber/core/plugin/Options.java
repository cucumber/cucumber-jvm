package io.cucumber.core.plugin;

import java.util.List;

public interface Options {
    Iterable<Plugin> plugins();

    boolean isMonochrome();

    boolean isStrict();

    interface Plugin {

        Class<? extends cucumber.api.Plugin> pluginClass();

        String argument();

        String pluginString();
    }
}
