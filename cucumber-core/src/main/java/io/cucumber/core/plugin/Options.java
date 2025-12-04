package io.cucumber.core.plugin;

import org.jspecify.annotations.Nullable;

public interface Options {

    Iterable<Plugin> plugins();

    boolean isMonochrome();

    boolean isWip();

    interface Plugin {

        Class<? extends io.cucumber.plugin.Plugin> pluginClass();

        @Nullable String argument();

        String pluginString();

    }

}
