package io.cucumber.core.plugin;

public interface Options {

    Iterable<Plugin> plugins();

    boolean isMonochrome();

    boolean isWip();

    interface Plugin {

        Class<? extends io.cucumber.plugin.Plugin> pluginClass();

        String argument();

        String pluginString();

    }

}
