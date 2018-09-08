package io.cucumber.core.plugin;

import cucumber.api.Plugin;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.StrictAware;
import io.cucumber.core.backend.StepDefinition;

import java.util.ArrayList;
import java.util.List;

public final class Plugins {
    private final List<Plugin> plugins;
    private boolean pluginNamesInstantiated;

    private final PluginFactory pluginFactory;
    private final EventPublisher eventPublisher;
    private final EventPublisher orderedEventPublisher;
    private final Options options;

    public Plugins(PluginFactory pluginFactory, EventPublisher eventPublisher, Options options) {
        this.pluginFactory = pluginFactory;
        this.eventPublisher = eventPublisher;
        this.orderedEventPublisher = createCanonicalOrderEventPublisher();
        this.options = options;
        this.plugins = createPlugins();
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
            for (Options.Plugin pluginOption : options.plugins()) {
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

    public StepDefinitionReporter stepDefinitionReporter() {
        return new StepDefinitionReporter() {
            @Override
            public void stepDefinition(StepDefinition stepDefinition) {
                for (Plugin plugin : getPlugins()) {
                    if (plugin instanceof StepDefinitionReporter) {
                        StepDefinitionReporter stepDefinitionReporter = (StepDefinitionReporter) plugin;
                        stepDefinitionReporter.stepDefinition(stepDefinition);
                    }
                }
            }
        };
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
            colorAware.setMonochrome(options.isMonochrome());
        }
    }

    private void setStrictOnStrictAwarePlugins(Plugin plugin) {
        if (plugin instanceof StrictAware) {
            StrictAware strictAware = (StrictAware) plugin;
            strictAware.setStrict(options.isStrict());
        }
    }

    private void setEventBusOnEventListenerPlugins(Plugin plugin) {
        if (plugin instanceof ConcurrentEventListener) {
            ConcurrentEventListener formatter = (ConcurrentEventListener) plugin;
            formatter.setEventPublisher(eventPublisher);
        } else if (plugin instanceof EventListener) {
            EventListener formatter = (EventListener) plugin;
            formatter.setEventPublisher(orderedEventPublisher);
        }
    }


}
