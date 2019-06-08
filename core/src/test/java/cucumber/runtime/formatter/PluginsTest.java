package cucumber.runtime.formatter;

import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.StrictAware;
import cucumber.runner.CanonicalOrderEventPublisher;
import cucumber.runtime.RuntimeOptions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.STRICT_STUBS;

public class PluginsTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(STRICT_STUBS);
    @Mock
    private EventPublisher rootEventPublisher;

    private PluginFactory pluginFactory = new PluginFactory();

    @Captor
    private ArgumentCaptor<EventPublisher> eventPublisher;

    @Test
    public void shouldSetStrictOnPlugin() {
        RuntimeOptions runtimeOptions = new RuntimeOptions("--strict");
        Plugins plugins = new Plugins(getSystemClassLoader(), pluginFactory, runtimeOptions);
        StrictAware plugin = Mockito.mock(StrictAware.class);
        plugins.addPlugin(plugin);
        verify(plugin).setStrict(true);
    }


    @Test
    public void shouldSetMonochromeOnPlugin() {
        RuntimeOptions runtimeOptions = new RuntimeOptions("--monochrome");
        Plugins plugins = new Plugins(getSystemClassLoader(), pluginFactory, runtimeOptions);
        ColorAware plugin = Mockito.mock(ColorAware.class);
        plugins.addPlugin(plugin);
        verify(plugin).setMonochrome(true);
    }


    @Test
    public void shouldSetConcurrentEventListener() {
        RuntimeOptions runtimeOptions = new RuntimeOptions(Collections.<String>emptyList());
        Plugins plugins = new Plugins(getSystemClassLoader(), pluginFactory, runtimeOptions);
        ConcurrentEventListener plugin = Mockito.mock(ConcurrentEventListener.class);
        plugins.addPlugin(plugin);
        plugins.setSerialEventBusOnEventListenerPlugins(rootEventPublisher);
        verify(plugin, times(1)).setEventPublisher(rootEventPublisher);
    }


    @Test
    public void shouldSetConcurrentEventListenerForSingleThread() {
        RuntimeOptions runtimeOptions = new RuntimeOptions(Collections.<String>emptyList());
        Plugins plugins = new Plugins(getSystemClassLoader(), pluginFactory, runtimeOptions);
        EventListener plugin = Mockito.mock(EventListener.class);
        plugins.addPlugin(plugin);
        plugins.setEventBusOnEventListenerPlugins(rootEventPublisher);
        verify(plugin, times(1)).setEventPublisher(rootEventPublisher);
    }


    @Test
    public void shouldSetNonConcurrentEventListenerForMultiThread() {
        RuntimeOptions runtimeOptions = new RuntimeOptions(Collections.<String>emptyList());
        Plugins plugins = new Plugins(getSystemClassLoader(), pluginFactory, runtimeOptions);
        EventListener plugin = Mockito.mock(EventListener.class);
        plugins.addPlugin(plugin);
        plugins.setSerialEventBusOnEventListenerPlugins(rootEventPublisher);
        verify(plugin, times(1)).setEventPublisher(eventPublisher.capture());
        assertEquals(CanonicalOrderEventPublisher.class, eventPublisher.getValue().getClass());
    }


    @Test
    public void shouldRegisterCanonicalOrderEventPublisherWithRootEventPublisher() {
        RuntimeOptions runtimeOptions = new RuntimeOptions(Collections.<String>emptyList());
        Plugins plugins = new Plugins(getSystemClassLoader(), pluginFactory, runtimeOptions);
        EventListener plugin = Mockito.mock(EventListener.class);
        plugins.addPlugin(plugin);
        plugins.setSerialEventBusOnEventListenerPlugins(rootEventPublisher);
        verify(rootEventPublisher, times(1)).registerHandlerFor(eq(Event.class), ArgumentMatchers.<EventHandler<Event>>any());
    }

}
