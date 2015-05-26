package cucumber.runtime;

import cucumber.runtime.xstream.LocalizedXStreams;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeGlueTest {
    @Test
    public void throws_duplicate_error_on_dupe_stepdefs() {
        RuntimeGlue glue = new RuntimeGlue(new UndefinedStepsTracker(), new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));

        StepDefinition a = mock(StepDefinition.class);
        when(a.getPattern()).thenReturn("hello");
        when(a.getLocation(true)).thenReturn("foo.bf:10");
        glue.addStepDefinition(a);

        StepDefinition b = mock(StepDefinition.class);
        when(b.getPattern()).thenReturn("hello");
        when(b.getLocation(true)).thenReturn("bar.bf:90");
        try {
            glue.addStepDefinition(b);
            fail("should have failed");
        } catch (DuplicateStepDefinitionException expected) {
            assertEquals("Duplicate step definitions in foo.bf:10 and bar.bf:90", expected.getMessage());
        }
    }

    @Test
    public void removes_glue_that_is_scenario_scoped() {
        // This test is a bit fragile - it is testing state, not behaviour.
        // But it was too much hassle creating a better test without refactoring RuntimeGlue
        // and probably some of its immediate collaborators... Aslak.

        RuntimeGlue glue = new RuntimeGlue(new UndefinedStepsTracker(), new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));

        StepDefinition sd = mock(StepDefinition.class);
        when(sd.isScenarioScoped()).thenReturn(true);
        when(sd.getPattern()).thenReturn("pattern");
        glue.addStepDefinition(sd);

        HookDefinition bh = mock(HookDefinition.class);
        when(bh.isScenarioScoped()).thenReturn(true);
        glue.addBeforeHook(bh, HookScope.SCENARIO);

        HookDefinition ah = mock(HookDefinition.class);
        when(ah.isScenarioScoped()).thenReturn(true);
        glue.addAfterHook(ah, HookScope.SCENARIO);

        assertEquals(1, glue.stepDefinitionsByPattern.size());
        assertEquals(1, glue.beforeHookDefinitions.size());
        assertEquals(1, glue.afterHookDefinitions.size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.stepDefinitionsByPattern.size());
        assertEquals(0, glue.beforeHookDefinitions.size());
        assertEquals(0, glue.afterHookDefinitions.size());
    }
}
