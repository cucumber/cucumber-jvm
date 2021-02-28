package io.cucumber.core.runner;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.ScenarioScoped;
import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.docstring.DocStringType;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.EventHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private final CachingGlue glue = new CachingGlue(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));

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
            () -> glue.prepareGlue(stepTypeRegistry));
        assertThat(exception.getMessage(), equalTo("Duplicate step definitions in foo.bf:10 and bar.bf:90"));
    }

    @Test
    void throws_on_duplicate_default_parameter_transformer() {
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());
        glue.addDefaultParameterTransformer(new MockedDefaultParameterTransformer());

        DuplicateDefaultParameterTransformers exception = assertThrows(
            DuplicateDefaultParameterTransformers.class,
            () -> glue.prepareGlue(stepTypeRegistry));
        assertThat(exception.getMessage(), equalTo("" +
                "There may not be more then one default parameter transformer. Found:\n" +
                " - mocked default parameter transformer\n" +
                " - mocked default parameter transformer\n"));
    }

    @Test
    void throws_on_duplicate_default_table_entry_transformer() {
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());
        glue.addDefaultDataTableEntryTransformer(new MockedDefaultDataTableEntryTransformer());

        DuplicateDefaultDataTableEntryTransformers exception = assertThrows(
            DuplicateDefaultDataTableEntryTransformers.class,
            () -> glue.prepareGlue(stepTypeRegistry));
        assertThat(exception.getMessage(), equalTo("" +
                "There may not be more then one default data table entry. Found:\n" +
                " - mocked default data table entry transformer\n" +
                " - mocked default data table entry transformer\n"));
    }

    @Test
    void throws_on_duplicate_default_table_cell_transformer() {
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());
        glue.addDefaultDataTableCellTransformer(new MockedDefaultDataTableCellTransformer());

        DuplicateDefaultDataTableCellTransformers exception = assertThrows(
            DuplicateDefaultDataTableCellTransformers.class,
            () -> glue.prepareGlue(stepTypeRegistry));
        assertThat(exception.getMessage(), equalTo("" +
                "There may not be more then one default table cell transformers. Found:\n" +
                " - mocked default data table cell transformer\n" +
                " - mocked default data table cell transformer\n"));
    }

    @Test
    void removes_glue_that_is_scenario_scoped() {
        // This test is a bit fragile - it is testing state, not behaviour.
        // But it was too much hassle creating a better test without refactoring
        // RuntimeGlue
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

        assertAll(
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
            () -> assertThat(glue.getDocStringTypeDefinitions().size(), is(equalTo(1))));

        glue.removeScenarioScopedGlue();

        assertAll(
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
            () -> assertThat(glue.getDocStringTypeDefinitions().size(), is(equalTo(0))));
    }

    @Test
    void returns_null_if_no_matching_steps_found() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition = new MockedStepDefinition("pattern1");
        glue.addStepDefinition(stepDefinition);

        URI uri = URI.create("file:path/to.feature");
        Step pickleStep = getPickleStep("pattern");
        assertThat(glue.stepDefinitionMatch(uri, pickleStep), is(nullValue()));
    }

    private static Step getPickleStep(String text) {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given " + text + "\n");

        return feature.getPickles().get(0).getSteps().get(0);
    }

    @Test
    void returns_match_from_cache_if_single_found() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1");
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2");
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        URI uri = URI.create("file:path/to.feature");
        String stepText = "pattern1";

        Step pickleStep1 = getPickleStep(stepText);

        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(uri, pickleStep1);
        assertThat(((CoreStepDefinition) pickleStepDefinitionMatch.getStepDefinition()).getStepDefinition(),
            is(equalTo(stepDefinition1)));

        // check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        Step pickleStep2 = getPickleStep(stepText);
        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(uri, pickleStep2);
        assertThat(((CoreStepDefinition) pickleStepDefinitionMatch2.getStepDefinition()).getStepDefinition(),
            is(equalTo(stepDefinition1)));
    }

    @Test
    void returns_match_from_cache_for_step_with_table() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1", DataTable.class);
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2", DataTable.class);
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        URI uri = URI.create("file:path/to.feature");
        String stepText = "pattern1";

        Step pickleStep1 = getPickleStepWithSingleCellTable(stepText, "cell 1");
        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(uri, pickleStep1);
        assertThat(((CoreStepDefinition) match1.getStepDefinition()).getStepDefinition(), is(equalTo(stepDefinition1)));

        // check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        // check arguments
        assertThat(((DataTable) match1.getArguments().get(0).getValue()).cell(0, 0), is(equalTo("cell 1")));

        // check second match
        Step pickleStep2 = getPickleStepWithSingleCellTable(stepText, "cell 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(uri, pickleStep2);

        // check arguments
        assertThat(((DataTable) match2.getArguments().get(0).getValue()).cell(0, 0), is(equalTo("cell 2")));
    }

    private static Step getPickleStepWithSingleCellTable(String stepText, String cell) {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given " + stepText + "\n" +
                "       | " + cell + " |\n");

        return feature.getPickles().get(0).getSteps().get(0);
    }

    @Test
    void returns_match_from_cache_for_ste_with_doc_string() throws AmbiguousStepDefinitionsException {
        StepDefinition stepDefinition1 = new MockedStepDefinition("^pattern1", String.class);
        StepDefinition stepDefinition2 = new MockedStepDefinition("^pattern2", String.class);
        glue.addStepDefinition(stepDefinition1);
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        URI uri = URI.create("file:path/to.feature");
        String stepText = "pattern1";

        Step pickleStep1 = getPickleStepWithDocString(stepText, "doc string 1");

        PickleStepDefinitionMatch match1 = glue.stepDefinitionMatch(uri, pickleStep1);
        assertThat(((CoreStepDefinition) match1.getStepDefinition()).getStepDefinition(), is(equalTo(stepDefinition1)));
        // check cache
        assertThat(glue.getStepPatternByStepText().get(stepText), is(equalTo(stepDefinition1.getPattern())));
        CoreStepDefinition coreStepDefinition = glue.getStepDefinitionsByPattern().get(stepDefinition1.getPattern());
        assertThat(coreStepDefinition.getStepDefinition(), is(equalTo(stepDefinition1)));

        // check arguments
        assertThat(match1.getArguments().get(0).getValue(), is(equalTo("doc string 1")));

        // check second match
        Step pickleStep2 = getPickleStepWithDocString(stepText, "doc string 2");
        PickleStepDefinitionMatch match2 = glue.stepDefinitionMatch(uri, pickleStep2);
        // check arguments
        assertThat(match2.getArguments().get(0).getValue(), is(equalTo("doc string 2")));
    }

    private static Step getPickleStepWithDocString(String stepText, String doc) {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given " + stepText + "\n" +
                "       \"\"\"\n" +
                "       " + doc + "\n" +
                "       \"\"\"\n");

        return feature.getPickles().get(0).getSteps().get(0);
    }

    @Test
    void returns_fresh_match_from_cache_after_evicting_scenario_scoped() throws AmbiguousStepDefinitionsException {
        URI uri = URI.create("file:path/to.feature");
        String stepText = "pattern1";
        Step pickleStep1 = getPickleStep(stepText);

        StepDefinition stepDefinition1 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition1);
        glue.prepareGlue(stepTypeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(uri, pickleStep1);
        assertThat(((CoreStepDefinition) pickleStepDefinitionMatch.getStepDefinition()).getStepDefinition(),
            is(equalTo(stepDefinition1)));

        glue.removeScenarioScopedGlue();

        StepDefinition stepDefinition2 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition2);
        glue.prepareGlue(stepTypeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(uri, pickleStep1);
        assertThat(((CoreStepDefinition) pickleStepDefinitionMatch2.getStepDefinition()).getStepDefinition(),
            is(equalTo(stepDefinition2)));
    }

    @Test
    void disposes_of_scenario_scoped_beans() {
        MockedScenarioScopedStepDefinition stepDefinition = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition);
        MockedScenarioScopedHookDefinition hookDefinition1 = new MockedScenarioScopedHookDefinition();
        glue.addBeforeHook(hookDefinition1);
        MockedScenarioScopedHookDefinition hookDefinition2 = new MockedScenarioScopedHookDefinition();
        glue.addAfterHook(hookDefinition2);
        MockedScenarioScopedHookDefinition hookDefinition3 = new MockedScenarioScopedHookDefinition();
        glue.addBeforeStepHook(hookDefinition3);
        MockedScenarioScopedHookDefinition hookDefinition4 = new MockedScenarioScopedHookDefinition();
        glue.addAfterStepHook(hookDefinition4);

        MockedDocStringTypeDefinition docStringType = new MockedDocStringTypeDefinition();
        glue.addDocStringType(docStringType);
        MockedDefaultDataTableEntryTransformer defaultDataTableEntryTransformer = new MockedDefaultDataTableEntryTransformer();
        glue.addDefaultDataTableEntryTransformer(defaultDataTableEntryTransformer);
        MockedDefaultDataTableCellTransformer defaultDataTableCellTransformer = new MockedDefaultDataTableCellTransformer();
        glue.addDefaultDataTableCellTransformer(defaultDataTableCellTransformer);
        MockedParameterTypeDefinition parameterType = new MockedParameterTypeDefinition();
        glue.addParameterType(parameterType);
        MockedDataTableTypeDefinition dataTableType = new MockedDataTableTypeDefinition();
        glue.addDataTableType(dataTableType);
        MockedDefaultParameterTransformer defaultParameterTransformer = new MockedDefaultParameterTransformer();
        glue.addDefaultParameterTransformer(defaultParameterTransformer);

        glue.prepareGlue(stepTypeRegistry);
        glue.removeScenarioScopedGlue();

        assertThat(stepDefinition.isDisposed(), is(true));
        assertThat(hookDefinition1.isDisposed(), is(true));
        assertThat(hookDefinition2.isDisposed(), is(true));
        assertThat(hookDefinition3.isDisposed(), is(true));
        assertThat(hookDefinition4.isDisposed(), is(true));
        assertThat(docStringType.isDisposed(), is(true));
        assertThat(defaultDataTableEntryTransformer.isDisposed(), is(true));
        assertThat(defaultDataTableCellTransformer.isDisposed(), is(true));
        assertThat(defaultParameterTransformer.isDisposed(), is(true));
        assertThat(parameterType.isDisposed(), is(true));
        assertThat(dataTableType.isDisposed(), is(true));
    }

    @Test
    void returns_no_match_after_evicting_scenario_scoped() throws AmbiguousStepDefinitionsException {
        URI uri = URI.create("file:path/to.feature");
        String stepText = "pattern1";
        Step pickleStep1 = getPickleStep(stepText);

        StepDefinition stepDefinition1 = new MockedScenarioScopedStepDefinition("^pattern1");
        glue.addStepDefinition(stepDefinition1);
        glue.prepareGlue(stepTypeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch = glue.stepDefinitionMatch(uri, pickleStep1);
        assertThat(((CoreStepDefinition) pickleStepDefinitionMatch.getStepDefinition()).getStepDefinition(),
            is(equalTo(stepDefinition1)));

        glue.removeScenarioScopedGlue();

        glue.prepareGlue(stepTypeRegistry);

        PickleStepDefinitionMatch pickleStepDefinitionMatch2 = glue.stepDefinitionMatch(uri, pickleStep1);
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

        URI uri = URI.create("file:path/to.feature");

        checkAmbiguousCalled(uri);
        // try again to verify if we don't cache when there is ambiguous step
        checkAmbiguousCalled(uri);
    }

    private void checkAmbiguousCalled(URI uri) {
        boolean ambiguousCalled = false;
        try {

            glue.stepDefinitionMatch(uri, getPickleStep("pattern1"));
        } catch (AmbiguousStepDefinitionsException e) {
            assertThat(e.getMatches().size(), is(equalTo(2)));
            ambiguousCalled = true;
        }
        assertTrue(ambiguousCalled);
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

    @Test
    public void emits_hook_messages_to_bus() {

        List<Messages.Envelope> events = new ArrayList<>();
        EventHandler<Messages.Envelope> messageEventHandler = e -> events.add(e);

        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        bus.registerHandlerFor(Messages.Envelope.class, messageEventHandler);
        CachingGlue glue = new CachingGlue(bus);

        glue.addBeforeHook(new MockedScenarioScopedHookDefinition());
        glue.addAfterHook(new MockedScenarioScopedHookDefinition());
        glue.addBeforeStepHook(new MockedScenarioScopedHookDefinition());
        glue.addAfterStepHook(new MockedScenarioScopedHookDefinition());

        glue.prepareGlue(stepTypeRegistry);
        assertThat(events.size(), is(4));
    }

    private static class MockedScenarioScopedStepDefinition extends StubStepDefinition implements ScenarioScoped {

        MockedScenarioScopedStepDefinition(String pattern, Type... types) {
            super(pattern, types);
        }

        MockedScenarioScopedStepDefinition(String pattern, boolean transposed, Type... types) {
            super(pattern, transposed, types);
        }
        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
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

        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
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

        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
        }

    }

    private static class MockedHookDefinition implements HookDefinition {

        private final int order;

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
        public void execute(TestCaseState state) {

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
        public void execute(TestCaseState state) {

        }

        @Override
        public String getTagExpression() {
            return "";
        }

        @Override
        public int getOrder() {
            return order;
        }

        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public Optional<SourceReference> getSourceReference() {
            return Optional.of(SourceReference.fromStackTraceElement(new StackTraceElement(
                "MockedScenarioScopedHookDefinition",
                "getSourceReference",
                "CachingGlueTest.java",
                582)));
        }
    }

    private static class MockedStepDefinition extends StubStepDefinition {

        MockedStepDefinition(String pattern, Type... types) {
            super(pattern, types);
        }

    }

    private static class MockedDefaultParameterTransformer
            implements DefaultParameterTransformerDefinition, ScenarioScoped {

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

        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
        }

    }

    private static class MockedDefaultDataTableCellTransformer
            implements DefaultDataTableCellTransformerDefinition, ScenarioScoped {

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

        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
        }

    }

    private static class MockedDefaultDataTableEntryTransformer
            implements DefaultDataTableEntryTransformerDefinition, ScenarioScoped {

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

        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
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

        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
        }

    }
}
