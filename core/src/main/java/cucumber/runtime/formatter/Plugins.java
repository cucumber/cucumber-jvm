package cucumber.runtime.formatter;

import cucumber.api.Plugin;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.StrictAware;
import cucumber.runner.CanonicalOrderEventPublisher;
import io.cucumber.core.options.PluginOptions;
import cucumber.runtime.Utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public final class Plugins {
    private final List<Plugin> plugins;
    private final ClassLoader classLoader;
    private boolean pluginNamesInstantiated;

    private final PluginFactory pluginFactory;
    private EventPublisher orderedEventPublisher;
    private final PluginOptions pluginOptions;

    public Plugins(ClassLoader classLoader, PluginFactory pluginFactory, PluginOptions pluginOptions) {
        this.classLoader = classLoader;
        this.pluginFactory = pluginFactory;
        this.pluginOptions = pluginOptions;
        this.plugins = createPlugins();
    }


    private EventPublisher getOrderedEventPublisher(EventPublisher eventPublisher) {
        // The ordered event publisher stores all events
        // so don't create it unless we need it.
        if(orderedEventPublisher == null){
            orderedEventPublisher = createCanonicalOrderEventPublisher(eventPublisher);
        }
        return orderedEventPublisher;
    }

    private static EventPublisher createCanonicalOrderEventPublisher(EventPublisher eventPublisher) {
        final CanonicalOrderEventPublisher canonicalOrderEventPublisher = new CanonicalOrderEventPublisher();
        eventPublisher.registerHandlerFor(Event.class, new EventHandler<Event>() {
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
            for (String pluginName : pluginOptions.getPluginNames()) {
                Plugin plugin = pluginFactory.create(pluginName);
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
        return pluginProxy(StepDefinitionReporter.class);
    }

    public void addPlugin(Plugin plugin) {
        addPlugin(plugins, plugin);
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
            strictAware.setStrict(pluginOptions.isStrict());
        }
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

    /**
     * Creates a dynamic proxy that multiplexes method invocations to all plugins of the same type.
     *
     * @param type proxy type
     * @param <T>  generic proxy type
     * @return a proxy
     */
    private <T> T pluginProxy(final Class<T> type) {
        Object proxy = Proxy.newProxyInstance(classLoader, new Class<?>[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
                for (Object plugin : getPlugins()) {
                    if (type.isInstance(plugin)) {
                        try {
                            Utils.invoke(plugin, method, 0, args);
                        } catch (Throwable t) {
                            if (!method.getName().equals("startOfScenarioLifeCycle") && !method.getName().equals("endOfScenarioLifeCycle")) {
                                // IntelliJ has its own formatter which doesn't yet implement these methods.
                                throw t;
                            }
                        }
                    }
                }
                return null;
            }
        });
        return type.cast(proxy);
    }


}
