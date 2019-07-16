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
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
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
import static org.mockito.Mockito.when;

public class CachingGlueTest {

    private final TypeRegistry typeRegistry = new TypeRegistry(ENGLISH);
    private CachingGlue glue = new CachingGlue(new TimeServiceEventBus(Clock.systemUTC()));

    @Test
    public void throws_duplicate_error_on_dupe_stepdefs() {
        StepDefinition a = mock(StepDefinition.class);
        when(a.getPattern()).thenReturn("hello");
        when(a.getLocation(true)).thenReturn("foo.bf:10");
        glue.addStepDefinition(a);

        StepDefinition b = mock(StepDefinition.class);
        when(b.getPattern()).thenReturn("hello");
        when(b.getLocation(true)).thenReturn("bar.bf:90");
        glue.addStepDefinition(b);

        try {
            glue.prepareGlue(typeRegistry);
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

        glue.addStepDefinition(new MockedScenarioScopedStepDefinition("pattern"));
        glue.addBeforeHook(new MockedScenarioScopedHookDefinition());
        glue.addAfterHook(new MockedScenarioScopedHookDefinition());
        glue.addDataTableType(new MockedDataTableTypeDefinition());
        glue.addParameterType(new MockedParemterTypeDefinition());

        glue.prepareGlue(typeRegistry);

        assertEquals(1, glue.getStepDefinitions().size());
        assertEquals(1, glue.getBeforeHooks().size());
        assertEquals(1, glue.getAfterHooks().size());
        assertEquals(1, glue.getDataTableTypeDefinitions().size());
        assertEquals(1, glue.getParameterTypeDefinitions().size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.getStepDefinitions().size());
        assertEquals(0, glue.getBeforeHooks().size());
        assertEquals(0, glue.getAfterHooks().size());
        assertEquals(0, glue.getDataTableTypeDefinitions().size());
        assertEquals(0, glue.getParameterTypeDefinitions().size());
    }

    @Test
    public void returns_null_if_no_matching_steps_found() {
        StepDefinition stepDefinition = new MockedStepDefinition("pattern1");
        glue.addStepDefinition(stepDefinition);

        String featurePath = "someFeature.feature";

        PickleStep pickleStep = getPickleStep("pattern");
        assertNull(glue.stepDefinitionMatch(featurePath, pickleStep));
    }

    @Test
    public void returns_match_from_cache_if_single_found() {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(typeRegistry);

        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        PickleStep pickleStep1 = getPickleStep(stepText);

        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertEquals(stepDefinition1, pickleStepDefinitionMatch.getStepDefinition());


        //check cache
        assertEquals(stepDefinition1.getPattern(), glue.getStepPatternByStepText().get(stepText));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertEquals(stepDefinition1, coreStepDefinition.getStepDefinition());

        PickleStep pickleStep2 = getPickleStep(stepText);
        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep2);
        assertEquals(stepDefinition1, pickleStepDefinitionMatch2.getStepDefinition());
    }

    @Test
    public void returns_match_from_cache_for_step_with_table() {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(typeRegistry);

        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        PickleStep pickleStep1 = getPickleStepWithSingleCellTable(stepText, "cell 1");
        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertEquals(stepDefinition1, match1.getStepDefinition());

        //check cache
        assertEquals(stepDefinition1.getPattern(), glue.getStepPatternByStepText().get(stepText));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertEquals(stepDefinition1, coreStepDefinition.getStepDefinition());

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
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(typeRegistry);

        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        PickleStep pickleStep1 = getPickleStepWithDocString(stepText, "doc string 1");

        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertEquals(stepDefinition1, match1.getStepDefinition());

        //check cache
        assertEquals(stepDefinition1.getPattern(), glue.getStepPatternByStepText().get(stepText));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertEquals(stepDefinition1, coreStepDefinition.getStepDefinition());

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
        glue.prepareGlue(typeRegistry);

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

    private static class MockedDataTableTypeDefinition implements DataTableTypeDefinition, ScenarioScoped {

        boolean disposed;

        @Override
        public DataTableType dataTableType() {
            return new DataTableType(Object.class, (DataTable table) -> new Object());
        }

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

    }
    private static class MockedParemterTypeDefinition implements ParameterTypeDefinition, ScenarioScoped {

        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public ParameterType<?> parameterType() {
            return new ParameterType<>("mock", "[ab]", Object.class, (String arg) -> new Object());
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
