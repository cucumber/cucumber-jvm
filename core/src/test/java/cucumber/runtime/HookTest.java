package cucumber.runtime;

import cucumber.api.Scenario;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookTest {
    private final static String ENGLISH = "en";

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     * TODO: ensure this is no longer needed with the alternate approach taken in Runtime
     * TODO: this test is rather brittle, since there's lots of mocking :(
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(ArgumentMatchers.<PickleTag>anyList())).thenReturn(true);

        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        final Backend backend = mock(Backend.class);
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return singletonList(backend);
            }
        };
        EventBus bus = new EventBus(TimeService.SYSTEM);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        RunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        Runner runner = runnerSupplier.get();
        runner.getGlue().addAfterHook(hook);
        PickleStep step = mock(PickleStep.class);
        PickleEvent pickleEvent = new PickleEvent("uri", new Pickle("name", ENGLISH, asList(step), Collections.<PickleTag>emptyList(), asList(mock(PickleLocation.class))));

        runner.runPickle(pickleEvent);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(hook).execute(ArgumentMatchers.<Scenario>any());
        inOrder.verify(backend).disposeWorld();
    }


}
