package io.cucumber.java;

import io.cucumber.java.en.Given;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public final class GlueAdaptorTestStepDefinitions {

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

    @AfterAll
    public static void afterAll() {

    }

    @BeforeAll
    public static void beforeAll() {

    }

    @DocStringType
    public @Nullable Object json(String docString) {
        return null;
    }

}
