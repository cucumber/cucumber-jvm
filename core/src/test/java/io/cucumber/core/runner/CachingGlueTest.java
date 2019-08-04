package io.cucumber.core.runner;

import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        assertAll("Checking Glue",
            () -> assertThat(glue.getStepDefinitions().size(), is(equalTo(1))),
            () -> assertThat(glue.getBeforeHooks().size(), is(equalTo(1))),
            () -> assertThat(glue.getAfterHooks().size(), is(equalTo(1))),
            () -> assertThat(glue.getBeforeStepHooks().size(), is(equalTo(1))),
            () -> assertThat(glue.getAfterStepHooks().size(), is(equalTo(1))),
            () -> assertThat(glue.getDataTableTypeDefinitions().size(), is(equalTo(1))),
            () -> assertThat(glue.getParameterTypeDefinitions().size(), is(equalTo(1))),
            () -> assertThat(glue.getDefaultParameterTransformers().size(), is(equalTo(1))),
            () -> assertThat(glue.getDefaultDataTableCellTransformers().size(), is(equalTo(1))),
            () -> assertThat(glue.getDefaultDataTableEntryTransformers().size(), is(equalTo(1)))
        );

        glue.removeScenarioScopedGlue();

        assertAll("Checking Glue",
            () -> assertThat(glue.getStepDefinitions().size(), is(equalTo(0))),
            () -> assertThat(glue.getBeforeHooks().size(), is(equalTo(0))),
            () -> assertThat(glue.getAfterHooks().size(), is(equalTo(0))),
            () -> assertThat(glue.getBeforeStepHooks().size(), is(equalTo(0))),
            () -> assertThat(glue.getAfterStepHooks().size(), is(equalTo(0))),
            () -> assertThat(glue.getDataTableTypeDefinitions().size(), is(equalTo(0))),
            () -> assertThat(glue.getParameterTypeDefinitions().size(), is(equalTo(0))),
            () -> assertThat(glue.getDefaultParameterTransformers().size(), is(equalTo(0))),
            () -> assertThat(glue.getDefaultDataTableCellTransformers().size(), is(equalTo(0))),
            () -> assertThat(glue.getDefaultDataTableEntryTransformers().size(), is(equalTo(0)))
        );
    }

    @Test
    public void returns_null_if_no_matching_steps_found() {
        StepDefinition stepDefinition = new MockedStepDefinition("pattern1");
        glue.addStepDefinition(stepDefinition);

        String featurePath = "someFeature.feature";

        PickleStep pickleStep = getPickleStep("pattern");
        assertThat(glue.stepDefinitionMatch(featurePath, pickleStep), is(nullValue()));
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
        assertThat(pickleStepDefinitionMatch.getStepDefinition(), is(equalTo(stepDefinition1)));


        //check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        PickleStep pickleStep2 = getPickleStep(stepText);
        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep2);
        assertThat(pickleStepDefinitionMatch2.getStepDefinition(), is(equalTo(stepDefinition1)));
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
        assertThat(match1.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check arguments
        assertThat(((DataTable) match1.getArguments().get(0).getValue()).cell(0, 0), is(equalTo("cell 1")));

        //check second match
        PickleStep pickleStep2 = getPickleStepWithSingleCellTable(stepText, "cell 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(featurePath, pickleStep2);

        //check arguments
        assertThat(((DataTable) match2.getArguments().get(0).getValue()).cell(0, 0), is(equalTo("cell 2")));
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
        assertThat(match1.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check arguments
        assertThat(match1.getArguments().get(0).getValue(), is(equalTo("doc string 1")));

        //check second match
        PickleStep pickleStep2 = getPickleStepWithDocString(stepText, "doc string 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(featurePath, pickleStep2);
        //check arguments
        assertThat(match2.getArguments().get(0).getValue(), is(equalTo("doc string 2")));
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
        assertThat(pickleStepDefinitionMatch.getStepDefinition(), is(equalTo(stepDefinition1)));

        glue.removeScenarioScopedGlue();

        StepDefinition stepDefinition2 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(typeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(pickleStepDefinitionMatch2.getStepDefinition(), is(equalTo(stepDefinition2)));
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
        assertThat(pickleStepDefinitionMatch.getStepDefinition(), is(equalTo(stepDefinition1)));

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
            assertThat(e.getMatches().size(), is(equalTo(2)));
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
