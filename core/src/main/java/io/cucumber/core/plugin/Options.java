package io.cucumber.core.plugin;

public interface Options {
    Iterable<Plugin> plugins();

    boolean isMonochrome();

    boolean isStrict();

    interface Plugin {

        Class<? extends io.cucumber.core.plugin.Plugin> pluginClass();

        String argument();

        String pluginString();
    }
}
