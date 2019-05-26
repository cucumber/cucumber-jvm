package io.cucumber.core.runner;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.options.Env;
import io.cucumber.core.options.RuntimeOptions;
import gherkin.events.PickleEvent;
import gherkin.pickles.Argument;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.time.Clock;
import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookTest {
    private final static String ENGLISH = "en";
    private final MultiLoader resoureceLoader = new MultiLoader(RuntimeOptions.class.getClassLoader());
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());
    private final RuntimeOptions runtimeOptions = new RuntimeOptions(resoureceLoader, Env.INSTANCE, emptyList());
    private final PickleStep pickleStep = new PickleStep("pattern1", Collections.<Argument>emptyList(), singletonList(new PickleLocation(2, 2)));
    private final PickleEvent pickleEvent = new PickleEvent("uri",
        new Pickle("scenario1", ENGLISH, singletonList(pickleStep), Collections.<PickleTag>emptyList(), singletonList(new PickleLocation(1, 1))));

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {

        Backend backend = mock(Backend.class);
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        final HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(ArgumentMatchers.<PickleTag>anyCollection())).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Glue glue = invocation.getArgument(0);
                glue.addBeforeHook(hook);
                return null;
            }
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.<URI>anyList());

        Runner runner = new Runner(bus, Collections.singleton(backend), objectFactory, runtimeOptions);

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(hook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(backend).disposeWorld();
    }
}
