package io.cucumber.core.plugin;

import io.cucumber.core.api.plugin.Plugin;
import io.cucumber.core.api.event.ConcurrentEventListener;
import io.cucumber.core.api.event.Event;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.plugin.ColorAware;
import io.cucumber.core.api.plugin.StrictAware;
import io.cucumber.core.options.PluginOptions;

import java.util.ArrayList;
import java.util.List;

public final class Plugins {
    private final List<Plugin> plugins;
    private boolean pluginNamesInstantiated;

    private final PluginFactory pluginFactory;
    private final EventPublisher eventPublisher;
    private EventPublisher orderedEventPublisher;
    private final PluginOptions pluginOptions;

    public Plugins(PluginFactory pluginFactory, EventPublisher eventPublisher, PluginOptions pluginOptions) {
        this.pluginFactory = pluginFactory;
        this.eventPublisher = eventPublisher;
        this.pluginOptions = pluginOptions;
        this.plugins = createPlugins();
    }


    private EventPublisher getOrderedEventPublisher() {
        // The ordered event publisher stores all events
        // so don't create it unless we need it.
        if(orderedEventPublisher == null){
            orderedEventPublisher = createCanonicalOrderEventPublisher();
        }
        return orderedEventPublisher;
    }

    private EventPublisher createCanonicalOrderEventPublisher() {
        final CanonicalOrderEventPublisher canonicalOrderEventPublisher = new CanonicalOrderEventPublisher();
        this.eventPublisher.registerHandlerFor(Event.class, new EventHandler<Event>() {
            @Override
            public void receive(Event event) {
                canonicalOrderEventPublisher.handle(event);
            }
        });
        return canonicalOrderEventPublisher;
    }

    private List<Plugin> createPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>();
        if (!pluginNamesInstantiated) {
            for (Options.Plugin pluginOption : pluginOptions.plugins()) {
                Plugin plugin = pluginFactory.create(pluginOption);
                addPlugin(plugins, plugin);
            }
            pluginNamesInstantiated = true;
        }
        return plugins;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void addPlugin(Plugin plugin) {
        addPlugin(plugins, plugin);
    }

    private void addPlugin(List<Plugin> plugins, Plugin plugin) {
        plugins.add(plugin);
        setMonochromeOnColorAwarePlugins(plugin);
        setStrictOnStrictAwarePlugins(plugin);
        setEventBusOnEventListenerPlugins(plugin);
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
            strictAware.setStrict(pluginOptions.isStrict());
        }
    }

    private void setEventBusOnEventListenerPlugins(Plugin plugin) {
        if (plugin instanceof ConcurrentEventListener) {
            ConcurrentEventListener formatter = (ConcurrentEventListener) plugin;
            formatter.setEventPublisher(eventPublisher);
        } else if (plugin instanceof EventListener) {
            EventListener formatter = (EventListener) plugin;
            formatter.setEventPublisher(getOrderedEventPublisher());
        }
    }


}
