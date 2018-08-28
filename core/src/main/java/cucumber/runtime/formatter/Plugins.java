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
import cucumber.runtime.RuntimeOptions;
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
    private final EventPublisher eventPublisher;
    private final EventPublisher orderedEventPublisher;
    private final RuntimeOptions runtimeOptions;

    public Plugins(ClassLoader classLoader, PluginFactory pluginFactory, EventPublisher eventPublisher, RuntimeOptions runtimeOptions) {
        this.classLoader = classLoader;
        this.pluginFactory = pluginFactory;
        this.eventPublisher = eventPublisher;
        this.orderedEventPublisher = createCanonicalOrderEventPublisher();
        this.runtimeOptions = runtimeOptions;
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
            for (String pluginName : runtimeOptions.getPluginFormatterNames()) {
                Plugin plugin = pluginFactory.create(pluginName);
                addPlugin(plugins, plugin);
            }
            for (String pluginName : runtimeOptions.getPluginStepDefinitionReporterNames()) {
                Plugin plugin = pluginFactory.create(pluginName);
                addPlugin(plugins, plugin);
            }
            for (String pluginName : runtimeOptions.getPluginSummaryPrinterNames()) {
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
        setEventBusOnEventListenerPlugins(plugin);
    }

    private void setMonochromeOnColorAwarePlugins(Plugin plugin) {
        if (plugin instanceof ColorAware) {
            ColorAware colorAware = (ColorAware) plugin;
            colorAware.setMonochrome(runtimeOptions.isMonochrome());
        }
    }

    private void setStrictOnStrictAwarePlugins(Plugin plugin) {
        if (plugin instanceof StrictAware) {
            StrictAware strictAware = (StrictAware) plugin;
            strictAware.setStrict(runtimeOptions.isStrict());
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
