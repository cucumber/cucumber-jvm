package io.cucumber.java;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.StepDefinition;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("NullAway") // TODO: Use AssertJ
class GlueAdaptorTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) new GlueAdaptorTestStepDefinitions();
        }
    };
    private final List<StepDefinition> stepDefinitions = new ArrayList<>();
    private final Matcher<StepDefinition> aStep = new CustomTypeSafeMatcher<StepDefinition>("a step") {
        @Override
        protected boolean matchesSafely(StepDefinition item) {
            return item.getPattern().equals("a step");
        }
    };
    private final Matcher<StepDefinition> repeated = new CustomTypeSafeMatcher<StepDefinition>("repeated") {
        @Override
        protected boolean matchesSafely(StepDefinition item) {
            return item.getPattern().equals("repeated");
        }
    };
    private @Nullable DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer;
    private @Nullable DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer;
    private @Nullable DefaultParameterTransformerDefinition defaultParameterTransformer;
    private @Nullable DataTableTypeDefinition dataTableTypeDefinition;
    private @Nullable ParameterTypeDefinition parameterTypeDefinition;
    private @Nullable HookDefinition afterStepHook;
    private @Nullable HookDefinition beforeStepHook;
    private @Nullable HookDefinition afterHook;
    private @Nullable HookDefinition beforeHook;
    private @Nullable StaticHookDefinition afterAllHook;
    private @Nullable StaticHookDefinition beforeAllHook;
    private @Nullable DocStringTypeDefinition docStringTypeDefinition;
    private final Glue container = new Glue() {
        @Override
        public void addBeforeAllHook(StaticHookDefinition beforeAllHook) {
            GlueAdaptorTest.this.beforeAllHook = beforeAllHook;
        }

        @Override
        public void addAfterAllHook(StaticHookDefinition afterAllHook) {
            GlueAdaptorTest.this.afterAllHook = afterAllHook;
        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            GlueAdaptorTest.this.stepDefinitions.add(stepDefinition);
        }

        @Override
        public void addBeforeHook(HookDefinition beforeHook) {
            GlueAdaptorTest.this.beforeHook = beforeHook;

        }

        @Override
        public void addAfterHook(HookDefinition afterHook) {
            GlueAdaptorTest.this.afterHook = afterHook;

        }

        @Override
        public void addBeforeStepHook(HookDefinition beforeStepHook) {
            GlueAdaptorTest.this.beforeStepHook = beforeStepHook;

        }

        @Override
        public void addAfterStepHook(HookDefinition afterStepHook) {
            GlueAdaptorTest.this.afterStepHook = afterStepHook;

        }

        @Override
        public void addParameterType(ParameterTypeDefinition parameterType) {
            GlueAdaptorTest.this.parameterTypeDefinition = parameterType;

        }

        @Override
        public void addDataTableType(DataTableTypeDefinition dataTableType) {
            GlueAdaptorTest.this.dataTableTypeDefinition = dataTableType;

        }

        @Override
        public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
            GlueAdaptorTest.this.defaultParameterTransformer = defaultParameterTransformer;

        }

        @Override
        public void addDefaultDataTableEntryTransformer(
                DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
        ) {
            GlueAdaptorTest.this.defaultDataTableEntryTransformer = defaultDataTableEntryTransformer;

        }

        @Override
        public void addDefaultDataTableCellTransformer(
                DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer
        ) {
            GlueAdaptorTest.this.defaultDataTableCellTransformer = defaultDataTableCellTransformer;

        }

        @Override
        public void addDocStringType(DocStringTypeDefinition docStringType) {
            GlueAdaptorTest.this.docStringTypeDefinition = docStringType;
        }
    };
    private final GlueAdaptor adaptor = new GlueAdaptor(lookup, container);

    @Test
    void creates_all_glue_steps() {
        MethodScanner.scan(GlueAdaptorTestStepDefinitions.class, adaptor::addDefinition);

        assertAll(
            () -> assertThat(stepDefinitions, containsInAnyOrder(aStep, repeated)),
            () -> assertThat(defaultDataTableCellTransformer, notNullValue()),
            () -> assertThat(defaultDataTableEntryTransformer, notNullValue()),
            () -> assertThat(defaultParameterTransformer, notNullValue()),
            () -> assertThat(dataTableTypeDefinition, notNullValue()),
            () -> assertThat(parameterTypeDefinition.parameterType().getRegexps(), is(singletonList("pattern"))),
            () -> assertThat(parameterTypeDefinition.parameterType().getName(), is("name")),
            () -> assertThat(parameterTypeDefinition.parameterType().preferForRegexpMatch(), is(true)),
            () -> assertThat(parameterTypeDefinition.parameterType().useForSnippets(), is(true)),
            () -> assertThat(parameterTypeDefinition.parameterType().useRegexpMatchAsStrongTypeHint(), is(false)),
            () -> assertThat(afterStepHook, notNullValue()),
            () -> assertThat(beforeStepHook, notNullValue()),
            () -> assertThat(afterHook, notNullValue()),
            () -> assertThat(beforeHook, notNullValue()),
            () -> assertThat(beforeAllHook, notNullValue()),
            () -> assertThat(afterAllHook, notNullValue()),
            () -> assertThat(docStringTypeDefinition, notNullValue()));
    }

}
