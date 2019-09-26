package io.cucumber.core.runner;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.Scenario;
import io.cucumber.core.backend.ScenarioScoped;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberStep;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.docstring.DocStringType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CachingGlueTest {

    private final StepTypeRegistry stepTypeRegistry = new StepTypeRegistry(ENGLISH);
    private CachingGlue glue = new CachingGlue(new TimeServiceEventBus(Clock.systemUTC()));

    private static CucumberStep getPickleStep(String text) {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given " + text + "\n"
        );

        return feature.getPickles().get(0).getSteps().get(0);
    }

    private static CucumberStep getPickleStepWithSingleCellTable(String stepText, String cell) {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given " + stepText + "\n" +
            "       | " + cell + " |\n"
        );

        return feature.getPickles().get(0).getSteps().get(0);
    }

    private static CucumberStep getPickleStepWithDocString(String stepText, String doc) {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given " + stepText + "\n" +
            "       \"\"\"\n" +
            "       " + doc + "\n" +
            "       \"\"\"\n"
        );

        return feature.getPickles().get(0).getSteps().get(0);
    }

    @Test
    void throws_duplicate_error_on_dupe_stepdefs() {
        StepDefinition a = mock(StepDefinition.class);
        when(a.getPattern()).thenReturn("hello");
        when(a.getLocation()).thenReturn("foo.bf:10");
        glue.addStepDefinition(a);

        StepDefinition b = mock(StepDefinition.class);
        when(b.getPattern()).thenReturn("hello");
        when(b.getLocation()).thenReturn("bar.bf:90");
        glue.addStepDefinition(b);

        DuplicateStepDefinitionException exception = assertThrows(
            DuplicateStepDefinitionException.class,
            () -> glue.prepareGlue(stepTypeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("Duplicate step definitions in foo.bf:10 and bar.bf:90"));
    }

    @Test
    void throws_on_duplicate_default_parameter_transformer() {
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());

        DuplicateDefaultParameterTransformers exception = assertThrows(
            DuplicateDefaultParameterTransformers.class,
            () -> glue.prepareGlue(stepTypeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("" +
            "There may not be more then one default parameter transformer. Found:\n" +
            " - mocked default parameter transformer\n" +
            " - mocked default parameter transformer\n"
        ));
    }

    @Test
    void throws_on_duplicate_default_table_entry_transformer() {
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());

        DuplicateDefaultDataTableEntryTransformers exception = assertThrows(
            DuplicateDefaultDataTableEntryTransformers.class,
            () -> glue.prepareGlue(stepTypeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("" +
            "There may not be more then one default data table entry. Found:\n" +
            " - mocked default data table entry transformer\n" +
            " - mocked default data table entry transformer\n"
        ));
    }

    @Test
    void throws_on_duplicate_default_table_cell_transformer() {
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());

        DuplicateDefaultDataTableCellTransformers exception = assertThrows(
            DuplicateDefaultDataTableCellTransformers.class,
            () -> glue.prepareGlue(stepTypeRegistry)
        );
        assertThat(exception.getMessage(), equalTo("" +
            "There may not be more then one default table cell transformers. Found:\n" +
            " - mocked default data table cell transformer\n" +
            " - mocked default data table cell transformer\n"
        ));
    }

    @Test
    void removes_glue_that_is_scenario_scoped() {
        // This test is a bit fragile - it is testing state, not behaviour.
        // But it was too much hassle creating a better test without refactoring RuntimeGlue
        // and probably some of its immediate collaborators... Aslak.

        glue.addStepDefinition(new MockedScenarioScopedStepDefinition("pattern"));
        glue.addBeforeHook(new MockedScenarioScopedHookDefinition());
        glue.addAfterHook(new MockedScenarioScopedHookDefinition());
        glue.addBeforeStepHook(new MockedScenarioScopedHookDefinition());
        glue.addAfterStepHook(new MockedScenarioScopedHookDefinition());
        glue.addParameterType(new MockedParameterTypeDefinition());
        glue.addDataTableType(new MockedDataTableTypeDefinition());
        glue.addDocStringType(new MockedDocStringTypeDefinition());
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());

        glue.prepareGlue(stepTypeRegistry);

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
            () -> assertThat(glue.getDefaultDataTableEntryTransformers().size(), is(equalTo(1))),
            () -> assertThat(glue.getDocStringTypeDefinitions().size(), is(equalTo(1)))
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
            () -> assertThat(glue.getDefaultDataTableEntryTransformers().size(), is(equalTo(0))),
            () -> assertThat(glue.getDocStringTypeDefinitions().size(), is(equalTo(0)))
        );
    }

    @Test
    void returns_null_if_no_matching_steps_found() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition = new MockedStepDefinition("pattern1");
        glue.addStepDefinition(stepDefinition);

        String featurePath = "someFeature.feature";

        CucumberStep pickleStep = getPickleStep("pattern");
        assertThat(glue.stepDefinitionMatch(featurePath, pickleStep), is(nullValue()));
    }

    @Test
    void returns_match_from_cache_if_single_found() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        CucumberStep pickleStep1 = getPickleStep(stepText);

        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(pickleStepDefinitionMatch.getStepDefinition(), is(equalTo(stepDefinition1)));


        //check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        CucumberStep pickleStep2 = getPickleStep(stepText);
        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep2);
        assertThat(pickleStepDefinitionMatch2.getStepDefinition(), is(equalTo(stepDefinition1)));
    }

    @Test
    void returns_match_from_cache_for_step_with_table() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        CucumberStep pickleStep1 = getPickleStepWithSingleCellTable(stepText, "cell 1");
        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(match1.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check arguments
        assertThat(((DataTable) match1.getArguments().get(0).getValue()).cell(0, 0), is(equalTo("cell 1")));

        //check second match
        CucumberStep pickleStep2 = getPickleStepWithSingleCellTable(stepText, "cell 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(featurePath, pickleStep2);

        //check arguments
        assertThat(((DataTable) match2.getArguments().get(0).getValue()).cell(0, 0), is(equalTo("cell 2")));
    }

    @Test
    void returns_match_from_cache_for_ste_with_doc_string() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        String featurePath = "someFeature.feature";
        String stepText = "pattern1";

        CucumberStep pickleStep1 = getPickleStepWithDocString(stepText, "doc string 1");

        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(match1.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        //check arguments
        assertThat(match1.getArguments().get(0).getValue(), is(equalTo("doc string 1")));

        //check second match
        CucumberStep pickleStep2 = getPickleStepWithDocString(stepText, "doc string 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(featurePath, pickleStep2);
        //check arguments
        assertThat(match2.getArguments().get(0).getValue(), is(equalTo("doc string 2")));
    }

    @Test
    void returns_fresh_match_from_cache_after_evicting_scenario_scoped() throws AmbiguousStepDefinitionsException {
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";
        CucumberStep pickleStep1 = getPickleStep(stepText);


        StepDefinition stepDefinition1 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition1);
        glue.prepareGlue(stepTypeRegistry);


        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(pickleStepDefinitionMatch.getStepDefinition(), is(equalTo(stepDefinition1)));

        glue.removeScenarioScopedGlue();

        StepDefinition stepDefinition2 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(pickleStepDefinitionMatch2.getStepDefinition(), is(equalTo(stepDefinition2)));
    }

    @Test
    void returns_no_match_after_evicting_scenario_scoped() throws AmbiguousStepDefinitionsException {
        String featurePath = "someFeature.feature";
        String stepText = "pattern1";
        CucumberStep pickleStep1 = getPickleStep(stepText);


        StepDefinition stepDefinition1 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition1);
        glue.prepareGlue(stepTypeRegistry);


        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(pickleStepDefinitionMatch.getStepDefinition(), is(equalTo(stepDefinition1)));

        glue.removeScenarioScopedGlue();

        glue.prepareGlue(stepTypeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(featurePath, pickleStep1);
        assertThat(pickleStepDefinitionMatch2, nullValue());
    }

    @Test
    void throws_ambiguous_steps_def_exception_when_many_patterns_match() {
        StepDefinition stepDefinition1 = new MockedStepDefinition("pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        StepDefinition stepDefinition3 = new MockedStepDefinition("^pattern[1,3]");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.addStepDefinition(stepDefinition3);
        glue.prepareGlue(stepTypeRegistry);

        String featurePath = "someFeature.feature";

        checkAmbiguousCalled(featurePath);
        //try again to verify if we don't cache when there is ambiguous step
        checkAmbiguousCalled(featurePath);
    }

    @Test
    void sorts_before_hooks_by_order() {
        HookDefinition hookDefinition1 = new MockedScenarioScopedHookDefinition(12);
        HookDefinition hookDefinition2 = new MockedScenarioScopedHookDefinition(13);
        HookDefinition hookDefinition3 = new MockedScenarioScopedHookDefinition(24);
        glue.addBeforeHook(hookDefinition1);
        glue.addBeforeHook(hookDefinition2);
        glue.addBeforeHook(hookDefinition3);

        List<HookDefinition> hooks = glue.getBeforeHooks()
            .stream()
            .map(CoreHookDefinition::getDelegate)
            .collect(Collectors.toList());

        assertThat(hooks, contains(hookDefinition1, hookDefinition2, hookDefinition3));
    }

    @Test
    void sorts_after_hooks_in_reverse_order() {
        HookDefinition hookDefinition1 = new MockedScenarioScopedHookDefinition(12);
        HookDefinition hookDefinition2 = new MockedScenarioScopedHookDefinition(12);
        HookDefinition hookDefinition3 = new MockedScenarioScopedHookDefinition(24);
        glue.addAfterHook(hookDefinition1);
        glue.addAfterHook(hookDefinition2);
        glue.addAfterHook(hookDefinition3);

        List<HookDefinition> hooks = glue.getAfterHooks()
            .stream()
            .map(CoreHookDefinition::getDelegate)
            .collect(Collectors.toList());

        assertThat(hooks, contains(hookDefinition3, hookDefinition2, hookDefinition1));
    }

    @Test
    void scenario_scoped_hooks_have_higher_order() {
        HookDefinition hookDefinition1 = new MockedScenarioScopedHookDefinition(12);
        HookDefinition hookDefinition2 = new MockedHookDefinition(12);
        HookDefinition hookDefinition3 = new MockedScenarioScopedHookDefinition(24);
        glue.addBeforeHook(hookDefinition1);
        glue.addBeforeHook(hookDefinition2);
        glue.addBeforeHook(hookDefinition3);

        List<HookDefinition> hooks = glue.getBeforeHooks()
            .stream()
            .map(CoreHookDefinition::getDelegate)
            .collect(Collectors.toList());

        assertThat(hooks, contains(hookDefinition2, hookDefinition1, hookDefinition3));
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

    private static class MockedScenarioScopedStepDefinition implements StepDefinition, ScenarioScoped {

        private final String pattern;

        MockedScenarioScopedStepDefinition(String pattern) {
            this.pattern = pattern;
        }

        MockedScenarioScopedStepDefinition() {
            this("mocked scenario scoped step definition");
        }

        @Override
        public String getLocation() {
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

        @Override
        public DataTableType dataTableType() {
            return new DataTableType(Object.class, (DataTable table) -> new Object());
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked data table type definition";
        }
    }

    private static class MockedParameterTypeDefinition implements ParameterTypeDefinition, ScenarioScoped {

        @Override
        public ParameterType<?> parameterType() {
            return new ParameterType<>("mock", "[ab]", Object.class, (String arg) -> new Object());
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked parameter type location";
        }
    }


    private static class MockedHookDefinition implements HookDefinition {

        private final int order;
        boolean disposed;

        MockedHookDefinition() {
            this(0);
        }

        MockedHookDefinition(int order) {
            this.order = order;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked hook definition";
        }

        @Override
        public void execute(Scenario scenario) {

        }

        @Override
        public String getTagExpression() {
            return "";
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class MockedScenarioScopedHookDefinition implements HookDefinition, ScenarioScoped {

        private final int order;

        MockedScenarioScopedHookDefinition() {
            this(0);
        }

        MockedScenarioScopedHookDefinition(int order) {
            this.order = order;
        }


        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked scenario scoped hook definition";
        }

        @Override
        public void execute(Scenario scenario) {

        }

        @Override
        public String getTagExpression() {
            return "";
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class MockedStepDefinition implements StepDefinition {

        private final String pattern;

        MockedStepDefinition(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public String getLocation() {
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

        @Override
        public ParameterByTypeTransformer parameterByTypeTransformer() {
            return (fromValue, toValueType) -> new Object();
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked default parameter transformer";
        }
    }

    private static class MockedDefaultDataTableCellTransformer implements DefaultDataTableCellTransformerDefinition, ScenarioScoped {

        @Override
        public TableCellByTypeTransformer tableCellByTypeTransformer() {
            return (value, cellType) -> new Object();
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked default data table cell transformer";
        }
    }

    private static class MockedDefaultDataTableEntryTransformer implements DefaultDataTableEntryTransformerDefinition, ScenarioScoped {

        @Override
        public boolean headersToProperties() {
            return false;
        }

        @Override
        public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
            return (entry, type, cellTransformer) -> new Object();
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked default data table entry transformer";
        }
    }

    private static class MockedDocStringTypeDefinition implements DocStringTypeDefinition, ScenarioScoped {

        @Override
        public DocStringType docStringType() {
            return new DocStringType(Object.class, "text/plain", content -> content);
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked default data table entry transformer";
        }

    }

}
