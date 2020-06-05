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
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.java.en.Given;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public class GlueAdaptorTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) GlueAdaptorTest.this;
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
    private DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer;
    private DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer;
    private DefaultParameterTransformerDefinition defaultParameterTransformer;
    private DataTableTypeDefinition dataTableTypeDefinition;
    private ParameterTypeDefinition parameterTypeDefinition;
    private HookDefinition afterStepHook;
    private HookDefinition beforeStepHook;
    private HookDefinition afterHook;
    private HookDefinition beforeHook;
    private DocStringTypeDefinition docStringTypeDefinition;
    private final Glue container = new Glue() {
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
        MethodScanner.scan(GlueAdaptorTest.class, adaptor::addDefinition);

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
            () -> assertThat(docStringTypeDefinition, notNullValue()));
    }

    @Given(value = "a step")
    @Given("repeated")
    public void step_definition() {

    }

    @DefaultDataTableCellTransformer
    public String default_data_table_cell_transformer(String fromValue, Type toValueType) {
        return "default_data_table_cell_transformer";
    }

    @DefaultDataTableEntryTransformer
    public String default_data_table_entry_transformer(Map<String, String> fromValue, Type toValueType) {
        return "default_data_table_entry_transformer";
    }

    @DefaultParameterTransformer
    public String default_parameter_transformer(String fromValue, Type toValueTYpe) {
        return "default_parameter_transformer";
    }

    @DataTableType
    public String data_table_type(String fromValue) {
        return "data_table_type";
    }

    @ParameterType(
            value = "pattern",
            name = "name",
            preferForRegexMatch = true,
            useForSnippets = true,
            useRegexpMatchAsStrongTypeHint = false)
    public String parameter_type(String fromValue) {
        return "parameter_type";
    }

    @AfterStep
    public void after_step() {

    }

    @BeforeStep
    public void before_step() {

    }

    @After
    public void after() {

    }

    @Before
    public void before() {

    }

    @DocStringType
    public Object json(String docString) {
        return null;
    }

}
