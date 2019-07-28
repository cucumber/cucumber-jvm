package io.cucumber.core.runner;

import gherkin.pickles.*;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.*;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import org.junit.Test;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        DuplicateStepDefinitionException exception = assertThrows(
            DuplicateStepDefinitionException.class,
            () -> glue.prepareGlue(typeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("Duplicate step definitions in foo.bf:10 and bar.bf:90"));
    }

    @Test
    public void throws_on_duplicate_default_parameter_transformer() {
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());

        DuplicateDefaultParameterTransformers exception = assertThrows(
            DuplicateDefaultParameterTransformers.class,
            () -> glue.prepareGlue(typeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("" +
            "There may not be more then one default parameter transformer. Found:\n" +
            " - mocked default parameter transformer\n" +
            " - mocked default parameter transformer\n"
        ));
    }

    @Test
    public void throws_on_duplicate_default_table_entry_transformer() {
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());

        DuplicateDefaultDataTableEntryTransformers exception = assertThrows(
            DuplicateDefaultDataTableEntryTransformers.class,
            () -> glue.prepareGlue(typeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("" +
            "There may not be more then one default data table entry. Found:\n" +
            " - mocked default data table entry transformer\n" +
            " - mocked default data table entry transformer\n"
        ));
    }

    @Test
    public void throws_on_duplicate_default_table_cell_transformer() {
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());

        DuplicateDefaultDataTableCellTransformers exception = assertThrows(
            DuplicateDefaultDataTableCellTransformers.class,
            () -> glue.prepareGlue(typeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("" +
            "There may not be more then one default table cell transformers. Found:\n" +
            " - mocked default data table cell transformer\n" +
            " - mocked default data table cell transformer\n"
        ));
    }


    @Test
    public void removes_glue_that_is_scenario_scoped() {
        // This test is a bit fragile - it is testing state, not behaviour.
        // But it was too much hassle creating a better test without refactoring RuntimeGlue
        // and probably some of its immediate collaborators... Aslak.

        glue.addStepDefinition(new MockedScenarioScopedStepDefinition("pattern"));
        glue.addBeforeHook(new MockedScenarioScopedHookDefinition());
        glue.addAfterHook(new MockedScenarioScopedHookDefinition());
        glue.addBeforeStepHook(new MockedScenarioScopedHookDefinition());
        glue.addAfterStepHook(new MockedScenarioScopedHookDefinition());
        glue.addDataTableType(new MockedDataTableTypeDefinition());
        glue.addParameterType(new MockedParameterTypeDefinition());
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());

        glue.prepareGlue(typeRegistry);

        assertEquals(1, glue.getStepDefinitions().size());
        assertEquals(1, glue.getBeforeHooks().size());
        assertEquals(1, glue.getAfterHooks().size());
        assertEquals(1, glue.getBeforeStepHooks().size());
        assertEquals(1, glue.getAfterStepHooks().size());
        assertEquals(1, glue.getDataTableTypeDefinitions().size());
        assertEquals(1, glue.getParameterTypeDefinitions().size());
        assertEquals(1, glue.getDefaultParameterTransformers().size());
        assertEquals(1, glue.getDefaultDataTableCellTransformers().size());
        assertEquals(1, glue.getDefaultDataTableEntryTransformers().size());

        glue.removeScenarioScopedGlue();

        assertEquals(0, glue.getStepDefinitions().size());
        assertEquals(0, glue.getBeforeHooks().size());
        assertEquals(0, glue.getAfterHooks().size());
        assertEquals(0, glue.getBeforeStepHooks().size());
        assertEquals(0, glue.getAfterStepHooks().size());
        assertEquals(0, glue.getDataTableTypeDefinitions().size());
        assertEquals(0, glue.getParameterTypeDefinitions().size());
        assertEquals(0, glue.getDefaultParameterTransformers().size());
        assertEquals(0, glue.getDefaultDataTableCellTransformers().size());
        assertEquals(0, glue.getDefaultDataTableEntryTransformers().size());
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

    @Test
    public void returns_fresh_match_from_cache_after_evicting_scenario_scoped() {
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";
        PickleStep pickleStep1 = getPickleStep(stepText);


        StepDefinition stepDefinition1 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition1);
        glue.prepareGlue(typeRegistry);


        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertEquals(stepDefinition1, pickleStepDefinitionMatch.getStepDefinition());

        glue.removeScenarioScopedGlue();

        StepDefinition stepDefinition2 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(typeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertEquals(stepDefinition2, pickleStepDefinitionMatch2.getStepDefinition());
    }


    @Test
    public void returns_no_match_after_evicting_scenario_scoped() {
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";
        PickleStep pickleStep1 = getPickleStep(stepText);


        StepDefinition stepDefinition1 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition1);
        glue.prepareGlue(typeRegistry);


        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertEquals(stepDefinition1, pickleStepDefinitionMatch.getStepDefinition());

        glue.removeScenarioScopedGlue();

        glue.prepareGlue(typeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(pickleStepDefinitionMatch2, nullValue());
    }

    private static PickleStep getPickleStepWithSingleCellTable(String stepText, String cell) {
        return new PickleStep(stepText, Collections.singletonList(new PickleTable(singletonList(new PickleRow(singletonList(new PickleCell(mock(PickleLocation.class), cell)))))), Collections.emptyList());
    }

    private static PickleStep getPickleStepWithDocString(String stepText, String doc) {
        return new PickleStep(stepText, Collections.singletonList(new PickleString(mock(PickleLocation.class), doc)), Collections.emptyList());
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
        return new PickleStep(text, Collections.emptyList(), Collections.emptyList());
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
        public String getLocation(boolean detail) {
            return "mocked data table type definition";
        }

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

    }

    private static class MockedParameterTypeDefinition implements ParameterTypeDefinition, ScenarioScoped {

        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public ParameterType<?> parameterType() {
            return new ParameterType<>("mock", "[ab]", Object.class, (String arg) -> new Object());
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked parameter type location";
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

    private static class MockedDefaultParameterTransformer implements DefaultParameterTransformerDefinition, ScenarioScoped {

        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public ParameterByTypeTransformer parameterByTypeTransformer() {
            return (fromValue, toValueType) -> new Object();
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked default parameter transformer";
        }
    }

    private static class MockedDefaultDataTableCellTransformer implements DefaultDataTableCellTransformerDefinition, ScenarioScoped {


        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public TableCellByTypeTransformer tableCellByTypeTransformer() {
            return new TableCellByTypeTransformer() {
                @Override
                public <T> T transform(String value, Class<T> cellType) {
                    return (T) new Object();
                }
            };
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked default data table cell transformer";
        }
    }

    private static class MockedDefaultDataTableEntryTransformer implements DefaultDataTableEntryTransformerDefinition, ScenarioScoped {
        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
            return new TableEntryByTypeTransformer() {
                @Override
                public <T> T transform(Map<String, String> entry, Class<T> type, TableCellByTypeTransformer cellTransformer) {
                    return (T) new Object();
                }
            };
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked default data table entry transformer";
        }
    }
}
