package io.cucumber.core.plugin;

import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventPublisher;

import java.util.ArrayList;
import java.util.List;

public final class Plugins {

    private final List<Plugin> plugins;
    private final PluginFactory pluginFactory;
    private final Options pluginOptions;
    private boolean pluginNamesInstantiated;
    private EventPublisher orderedEventPublisher;

    public Plugins(PluginFactory pluginFactory, Options pluginOptions) {
        this.pluginFactory = pluginFactory;
        this.pluginOptions = pluginOptions;
        this.plugins = createPlugins();
    }

    private List<Plugin> createPlugins() {
        List<Plugin> plugins = new ArrayList<>();
        if (!pluginNamesInstantiated) {
            for (Options.Plugin pluginOption : pluginOptions.plugins()) {
                Plugin plugin = pluginFactory.create(pluginOption);
                addPlugin(plugins, plugin);
            }
            pluginNamesInstantiated = true;
        }
        return plugins;
    }

    private void addPlugin(List<Plugin> plugins, Plugin plugin) {
        plugins.add(plugin);
        setMonochromeOnColorAwarePlugins(plugin);
        setStrictOnStrictAwarePlugins(plugin);
    }

    private void setMonochromeOnColorAwarePlugins(Plugin plugin) {
        if (plugin instanceof ColorAware) {
            ColorAware colorAware = (ColorAware) plugin;
            colorAware.setMonochrome(pluginOptions.isMonochrome());
        }
    }

    private void setStrictOnStrictAwarePlugins(Plugin plugin) {
        if (plugin instanceof StrictAware) {
            StrictAware strictAware = (StrictAware) plugin;
            strictAware.setStrict(true);
        }
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void addPlugin(Plugin plugin) {
        addPlugin(plugins, plugin);
    }

    public void setEventBusOnEventListenerPlugins(EventPublisher eventPublisher) {
        for (Plugin plugin : plugins) {
            if (plugin instanceof ConcurrentEventListener) {
                ((ConcurrentEventListener) plugin).setEventPublisher(eventPublisher);
            } else if (plugin instanceof EventListener) {
                ((EventListener) plugin).setEventPublisher(eventPublisher);
            }
        }
    }

    public void setSerialEventBusOnEventListenerPlugins(EventPublisher eventPublisher) {
        for (Plugin plugin : plugins) {
            if (plugin instanceof ConcurrentEventListener) {
                ((ConcurrentEventListener) plugin).setEventPublisher(eventPublisher);
            } else if (plugin instanceof EventListener) {
                EventPublisher orderedEventPublisher = getOrderedEventPublisher(eventPublisher);
                ((EventListener) plugin).setEventPublisher(orderedEventPublisher);
            }
        }
    }

    private EventPublisher getOrderedEventPublisher(EventPublisher eventPublisher) {
        // The ordered event publisher stores all events
        // so don't create it unless we need it.
        if (orderedEventPublisher == null) {
            orderedEventPublisher = createCanonicalOrderEventPublisher(eventPublisher);
        }
        return orderedEventPublisher;
    }

    private static EventPublisher createCanonicalOrderEventPublisher(EventPublisher eventPublisher) {
        final CanonicalOrderEventPublisher canonicalOrderEventPublisher = new CanonicalOrderEventPublisher();
        eventPublisher.registerHandlerFor(Event.class, canonicalOrderEventPublisher::handle);
        return canonicalOrderEventPublisher;
    }

}
