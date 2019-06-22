package io.cucumber.core.plugin;

import io.cucumber.core.api.event.ConcurrentEventListener;
import io.cucumber.core.api.event.Event;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.plugin.ColorAware;
import io.cucumber.core.api.plugin.StrictAware;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.STRICT_STUBS;

public class PluginsTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(STRICT_STUBS);

    @Mock
    private EventPublisher rootEventPublisher;

    @Captor
    private ArgumentCaptor<EventPublisher> eventPublisher;

    private PluginFactory pluginFactory = new PluginFactory();

    @Test
    public void shouldSetStrictOnPlugin() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        StrictAware plugin = mock(StrictAware.class);
        plugins.addPlugin(plugin);
        verify(plugin).setStrict(false);
    }

    @Test
    public void shouldSetMonochromeOnPlugin() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        ColorAware plugin = mock(ColorAware.class);
        plugins.addPlugin(plugin);
        verify(plugin).setMonochrome(false);
    }

    @Test
    public void shouldSetConcurrentEventListener() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        ConcurrentEventListener plugin = mock(ConcurrentEventListener.class);
        plugins.addPlugin(plugin);
        plugins.setEventBusOnEventListenerPlugins(rootEventPublisher);
        verify(plugin, times(1)).setEventPublisher(rootEventPublisher);
    }

    @Test
    public void shouldSetNonConcurrentEventListener() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        EventListener plugin = mock(EventListener.class);
        plugins.addPlugin(plugin);
        plugins.setSerialEventBusOnEventListenerPlugins(rootEventPublisher);
        verify(plugin, times(1)).setEventPublisher(eventPublisher.capture());
        assertEquals(CanonicalOrderEventPublisher.class, eventPublisher.getValue().getClass());
    }

    @Test
    public void shouldRegisterCanonicalOrderEventPublisherWithRootEventPublisher() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        Plugins plugins = new Plugins(pluginFactory, runtimeOptions);
        EventListener plugin = mock(EventListener.class);
        plugins.addPlugin(plugin);
        plugins.setSerialEventBusOnEventListenerPlugins(rootEventPublisher);
        verify(rootEventPublisher, times(1)).registerHandlerFor(eq(Event.class), ArgumentMatchers.any());
    }

}