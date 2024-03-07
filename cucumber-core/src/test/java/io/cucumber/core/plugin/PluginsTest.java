package io.cucumber.core.plugin;

import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginsTest {

    private final PluginFactory pluginFactory = new PluginFactory();

    @Test
    void shouldSetStrictOnPlugin() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        MockStrictAware plugin = new MockStrictAware();
        plugins.addPlugin(plugin);
        assertTrue(plugin.strict);
    }

    @Test
    void shouldSetMonochromeOnPlugin() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        MockColorAware plugin = new MockColorAware();
        plugins.addPlugin(plugin);
        assertFalse(plugin.monochrome);
    }

    @Test
    void shouldSetConcurrentEventListener() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        MockConcurrentEventListener plugin = new MockConcurrentEventListener();
        EventPublisher rootEventPublisher = new MockEventPublisher();
        plugins.addPlugin(plugin);
        plugins.setEventBusOnEventListenerPlugins(rootEventPublisher);

        assertIterableEquals(List.of(rootEventPublisher), plugin.eventPublishers);
    }

    @Test
    void shouldSetNonConcurrentEventListener() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        MockEventListener plugin = new MockEventListener();
        EventPublisher rootEventPublisher = new MockEventPublisher();
        plugins.addPlugin(plugin);
        plugins.setSerialEventBusOnEventListenerPlugins(rootEventPublisher);

        assertEquals(1, plugin.eventPublishers.size());
        assertInstanceOf(CanonicalOrderEventPublisher.class, plugin.eventPublishers.get(0));
    }

    @Test
    void shouldRegisterCanonicalOrderEventPublisherWithRootEventPublisher() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        MockEventListener plugin = new MockEventListener();
        MockEventPublisher rootEventPublisher = new MockEventPublisher();
        plugins.addPlugin(plugin);
        plugins.setSerialEventBusOnEventListenerPlugins(rootEventPublisher);

        List<EventHandler<?>> eventHandlers = rootEventPublisher.handlers.get(Event.class);
        assertNotNull(eventHandlers);
        assertEquals(1, eventHandlers.size());
    }

    @SuppressWarnings("deprecation")
    private static class MockStrictAware implements StrictAware {
        Boolean strict;
        @Override
        public void setStrict(boolean strict) {
            this.strict = strict;
        }
    }

    private static class MockColorAware implements ColorAware {
        Boolean monochrome;
        @Override
        public void setMonochrome(boolean monochrome) {
            this.monochrome = monochrome;
        }
    }

    private static class MockConcurrentEventListener implements ConcurrentEventListener {
        final List<EventPublisher> eventPublishers = new ArrayList<>();
        @Override
        public void setEventPublisher(EventPublisher publisher) {
            eventPublishers.add(publisher);
        }
    }

    private static class MockEventListener implements EventListener {
        final List<EventPublisher> eventPublishers = new ArrayList<>();
        @Override
        public void setEventPublisher(EventPublisher publisher) {
            eventPublishers.add(publisher);
        }
    }

    private static class MockEventPublisher implements EventPublisher {
        final Map<Class<?>, List<EventHandler<?>>> handlers = new HashMap<>();
        @Override
        public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
            List<EventHandler<?>> eventHandlers = handlers.computeIfAbsent(eventType, key -> new ArrayList<>());
            eventHandlers.add(handler);
        }

        @Override
        public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }
    }
}
