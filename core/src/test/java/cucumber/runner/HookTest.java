package cucumber.runner;

import cucumber.api.Scenario;
import cucumber.runtime.Backend;
import io.cucumber.core.options.RuntimeOptions;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
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
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookTest {
    private final static String ENGLISH = "en";
    private final EventBus bus = new TimeServiceEventBus(TimeService.SYSTEM);
    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final PickleStep pickleStep = new PickleStep("pattern1", Collections.<Argument>emptyList(), singletonList(new PickleLocation(2, 2)));
    private final PickleEvent pickleEvent = new PickleEvent("uri",
        new Pickle("scenario1", ENGLISH, singletonList(pickleStep), Collections.<PickleTag>emptyList(), singletonList(new PickleLocation(1, 1))));

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {

        Backend backend = mock(Backend.class);
        final HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(ArgumentMatchers.<PickleTag>anyCollection())).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                cucumber.runtime.Glue glue = invocation.getArgument(0);
                glue.addBeforeHook(hook);
                return null;
            }
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.<URI>anyList());

        Runner runner = new Runner(bus, Collections.singleton(backend), runtimeOptions);

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(hook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(backend).disposeWorld();
    }
}
