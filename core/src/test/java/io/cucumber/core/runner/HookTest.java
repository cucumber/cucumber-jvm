package io.cucumber.core.runner;

import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.time.Clock;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookTest {
    private final static String ENGLISH = "en";
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());
    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final PickleStep pickleStep = new PickleStep("pattern1", Collections.emptyList(), singletonList(new PickleLocation(2, 2)));
    private final PickleEvent pickleEvent = new PickleEvent("uri",
        new Pickle("scenario1", ENGLISH, singletonList(pickleStep), Collections.emptyList(), singletonList(new PickleLocation(1, 1))));

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        Backend backend = mock(Backend.class);
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        final HookDefinition hook = mock(HookDefinition.class);
        TypeRegistryConfigurer typeRegistryConfigurer = mock(TypeRegistryConfigurer.class);
        when(hook.matches(ArgumentMatchers.anyCollection())).thenReturn(true);

        doAnswer(invocation -> {
            Glue glue = invocation.getArgument(0);
            glue.addBeforeHook(hook);
            return null;
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.anyList());

        Runner runner = new Runner(bus, Collections.singleton(backend), objectFactory, typeRegistryConfigurer, runtimeOptions);

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(hook).execute(ArgumentMatchers.any());
        inOrder.verify(backend).disposeWorld();
    }
}
