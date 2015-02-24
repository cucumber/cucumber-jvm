package cucumber.runtime;

import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeGlueTest {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();

    @Test
    public void throws_duplicate_error_when_dupe_stepdefs_are_added() {
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

    @Test(expected = AmbiguousStepDefinitionsException.class)
    public void throws_ambiguous_error_when_step_matches_two_stepdefs() {
        RuntimeGlue glue = new RuntimeGlue(new UndefinedStepsTracker(), new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));
        Step step = new Step(NO_COMMENTS, "Given ", "three blind mice", 1, null, null);

        StepDefinition a = mock(StepDefinition.class);
        when(a.getPattern()).thenReturn("foo");
        when(a.matchedArguments(step)).thenReturn(new ArrayList<Argument>());

        StepDefinition b = mock(StepDefinition.class);
        when(b.getPattern()).thenReturn("bar");
        when(b.matchedArguments(step)).thenReturn(new ArrayList<Argument>());

        glue.addStepDefinition(a);
        glue.addStepDefinition(b);

        glue.stepDefinitionMatch("some.feature", step, new I18n("en"));
    }

    @Test
    public void does_not_throw_ambiguous_error_when_step_matches_one_stepdef() {
        RuntimeGlue glue = new RuntimeGlue(new UndefinedStepsTracker(), new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));
        Step step = new Step(NO_COMMENTS, "Given ", "three blind mice", 1, null, null);

        StepDefinition a = mock(StepDefinition.class);
        when(a.getPattern()).thenReturn("foo");
        when(a.matchedArguments(step)).thenReturn(new ArrayList<Argument>());

        glue.addStepDefinition(a);

        StepDefinitionMatch match = glue.stepDefinitionMatch("some.feature", step, new I18n("en"));
        assertNotNull(match);
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
        glue.addBeforeHook(bh);

        HookDefinition ah = mock(HookDefinition.class);
        when(ah.isScenarioScoped()).thenReturn(true);
        glue.addAfterHook(ah);

        assertEquals(1, glue.stepDefinitionsByPattern.size());
        assertEquals(1, glue.beforeHooks.size());
        assertEquals(1, glue.afterHooks.size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.stepDefinitionsByPattern.size());
        assertEquals(0, glue.beforeHooks.size());
        assertEquals(0, glue.afterHooks.size());
    }
}
