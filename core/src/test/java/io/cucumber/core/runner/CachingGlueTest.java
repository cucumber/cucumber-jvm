package io.cucumber.core.runner;

import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.DuplicateStepDefinitionException;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.datatable.DataTable;
import org.junit.Test;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CachingGlueTest {

    private final TypeRegistry typeRegistry = new TypeRegistry(ENGLISH);
    private CachingGlue glue = new CachingGlue(new TimeServiceEventBus(Clock.systemUTC()), typeRegistry);

    @Test
    public void throws_duplicate_error_on_dupe_stepdefs() {
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

        StepDefinition sd = spy(new MockedScenarioScopedStepDefinition());
        when(sd.getPattern()).thenReturn("pattern");
        glue.addStepDefinition(sd);

        HookDefinition bh = spy(new MockedScenarioScopedHookDefinition());
        glue.addBeforeHook(bh);

        HookDefinition ah = spy(new MockedScenarioScopedHookDefinition());
        glue.addAfterHook(ah);

        assertEquals(1, glue.stepDefinitionsByPattern.size());
        assertEquals(1, glue.beforeHooks.size());
        assertEquals(1, glue.afterHooks.size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.stepDefinitionsByPattern.size());
        assertEquals(0, glue.beforeHooks.size());
        assertEquals(0, glue.afterHooks.size());
    }

    @Test
    public void removes_scenario_scoped_cache_entries() {
        StepDefinition sd = new MockedScenarioScopedStepDefinition("pattern");
        glue.addStepDefinition(sd);
        String featurePath = "someFeature.feature";

        String stepText = "pattern";
        PickleStep pickleStep1 = getPickleStep(stepText);
        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        CoreStepDefinition coreStepDefinition = (CoreStepDefinition) pickleStepDefinitionMatch.getStepDefinition();
        assertEquals(sd, coreStepDefinition.getStepDefinition());

        assertEquals(1, glue.stepDefinitionsByStepText.size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.stepDefinitionsByStepText.size());
    }

    @Test
    public void returns_null_if_no_matching_steps_found() {
        StepDefinition stepDefinition = spy(new MockedStepDefinition("pattern1"));
        glue.addStepDefinition(stepDefinition);
        String featurePath = "someFeature.feature";

        PickleStep pickleStep = getPickleStep("pattern");
        assertNull(glue.stepDefinitionMatch(featurePath, pickleStep));
    }

    @Test
    public void returns_match_from_cache_if_single_found() {
        StepDefinition stepDefinition1 = spy(new MockedStepDefinition("^pattern1"));
        StepDefinition stepDefinition2 = spy(new MockedStepDefinition("^pattern2"));
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        PickleStep pickleStep1 = getPickleStep(stepText);

        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        CoreStepDefinition coreStepDefinition = (CoreStepDefinition) pickleStepDefinitionMatch.getStepDefinition();
        assertEquals(stepDefinition1, coreStepDefinition.getStepDefinition());


        //check cache
        CoreStepDefinition entry = glue.stepDefinitionsByStepText.get(stepText);
        assertEquals(stepDefinition1, entry.getStepDefinition());

        PickleStep pickleStep2 = getPickleStep(stepText);
        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep2);
        CoreStepDefinition coreStepDefinition2 = (CoreStepDefinition) pickleStepDefinitionMatch2.getStepDefinition();
        assertEquals(stepDefinition1, coreStepDefinition2.getStepDefinition());
    }

    @Test
    public void returns_match_from_cache_for_step_with_table() {
        StepDefinition stepDefinition1 = spy(new MockedStepDefinition("^pattern1"));
        StepDefinition stepDefinition2 = spy(new MockedStepDefinition("^pattern2"));
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        PickleStep pickleStep1 = getPickleStepWithSingleCellTable(stepText, "cell 1");
        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        CoreStepDefinition coreStepDefinition = (CoreStepDefinition) match1.getStepDefinition();

        assertEquals(stepDefinition1, coreStepDefinition.getStepDefinition());

        //check cache
        CoreStepDefinition entry = glue.stepDefinitionsByStepText.get(stepText);
        assertEquals(stepDefinition1, entry.getStepDefinition());

        //check arguments
        assertEquals("cell 1", ((DataTable) match1.getArguments().get(0).getValue()).cell(0, 0));

        //check second match
        PickleStep pickleStep2 = getPickleStepWithSingleCellTable(stepText, "cell 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(featurePath, pickleStep2);

        //check arguments
        assertEquals("cell 2", ((DataTable) match2.getArguments().get(0).getValue()).cell(0, 0));
    }

    @Test
    public void returns_match_from_cache_for_ste_with_doc_string() {
        StepDefinition stepDefinition1 = spy(new MockedStepDefinition("^pattern1"));
        StepDefinition stepDefinition2 = spy(new MockedStepDefinition("^pattern2"));
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        PickleStep pickleStep1 = getPickleStepWithDocString(stepText, "doc string 1");

        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        CoreStepDefinition coreStepDefinition = (CoreStepDefinition) match1.getStepDefinition();

        assertEquals(stepDefinition1, coreStepDefinition.getStepDefinition());

        //check cache
        CoreStepDefinition entry = glue.stepDefinitionsByStepText.get(stepText);
        assertEquals(stepDefinition1, entry.getStepDefinition());

        //check arguments
        assertEquals("doc string 1", match1.getArguments().get(0).getValue());

        //check second match
        PickleStep pickleStep2 = getPickleStepWithDocString(stepText, "doc string 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(featurePath, pickleStep2);
        //check arguments
        assertEquals("doc string 2", match2.getArguments().get(0).getValue());


    }


    private static PickleStep getPickleStepWithSingleCellTable(String stepText, String cell) {
        return new PickleStep(stepText, Collections.<Argument>singletonList(new PickleTable(singletonList(new PickleRow(singletonList(new PickleCell(mock(PickleLocation.class), cell)))))), Collections.<PickleLocation>emptyList());
    }

    private static PickleStep getPickleStepWithDocString(String stepText, String doc) {
        return new PickleStep(stepText, Collections.<Argument>singletonList(new PickleString(mock(PickleLocation.class), doc)), Collections.<PickleLocation>emptyList());
    }

    @Test
    public void throws_ambiguous_steps_def_exception_when_many_patterns_match() {
        StepDefinition stepDefinition1 = new MockedStepDefinition("pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        StepDefinition stepDefinition3 = new MockedStepDefinition("^pattern[1,3]");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.addStepDefinition(stepDefinition3);
        String featurePath = "someFeature.feature";

        checkAmbiguousCalled(featurePath);
        //try again to verify if we don't cache when there is ambiguous step
        checkAmbiguousCalled(featurePath);
    }

    private void checkAmbiguousCalled(String featurePath) {
        boolean ambiguousCalled = false;
        try {

            glue.stepDefinitionMatch(featurePath, getPickleStep("pattern1"));
        } catch (AmbiguousStepDefinitionsException e) {
            assertEquals(2, e.getMatches().size());
            ambiguousCalled = true;
        }
        assertTrue(ambiguousCalled);
    }

    private static PickleStep getPickleStep(String text) {
        return new PickleStep(text, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
    }

    private static class MockedScenarioScopedStepDefinition implements StepDefinition, ScenarioScoped {

        private final String pattern;

        MockedScenarioScopedStepDefinition(String pattern) {
            this.pattern = pattern;
        }

        MockedScenarioScopedStepDefinition() {
            this("mocked scenario scoped step definition");
        }

        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked scenario scoped step definition";
        }

        @Override
        public void execute(Object[] args) {

        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return null;
        }

        @Override
        public String getPattern() {
            return pattern;
        }

    }

    private static class MockedScenarioScopedHookDefinition implements HookDefinition, ScenarioScoped {

        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked scenario scoped hook definition";
        }

        @Override
        public void execute(Scenario scenario) {

        }

        @Override
        public boolean matches(Collection<PickleTag> tags) {
            return true;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    private static class MockedStepDefinition implements StepDefinition {

        private final String pattern;

        MockedStepDefinition(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked step location";
        }

        @Override
        public void execute(Object[] args) {

        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return null;
        }

        @Override
        public String getPattern() {
            return pattern;
        }
    }
}
