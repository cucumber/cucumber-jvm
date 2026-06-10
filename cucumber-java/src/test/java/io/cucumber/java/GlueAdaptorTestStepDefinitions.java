package io.cucumber.java;

import io.cucumber.java.en.Given;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public final class GlueAdaptorTestStepDefinitions {

    @Given(value = "a step")
    @Given("repeated")
    void step_definition() {

    }

    @DefaultDataTableCellTransformer
    String default_data_table_cell_transformer(String fromValue, Type toValueType) {
        return "default_data_table_cell_transformer";
    }

    @DefaultDataTableEntryTransformer
    String default_data_table_entry_transformer(Map<String, String> fromValue, Type toValueType) {
        return "default_data_table_entry_transformer";
    }

    @DefaultParameterTransformer
    String default_parameter_transformer(String fromValue, Type toValueTYpe) {
        return "default_parameter_transformer";
    }

    @DataTableType
    String data_table_type(String fromValue) {
        return "data_table_type";
    }

    @ParameterType(
            value = "pattern",
            name = "name",
            preferForRegexMatch = true,
            useForSnippets = true,
            useRegexpMatchAsStrongTypeHint = false)
    String parameter_type(String fromValue) {
        return "parameter_type";
    }

    @AfterStep(name = "after-step")
    void after_step() {

    }

    @BeforeStep(name = "before-step")
    void before_step() {

    }

    @After(name = "after")
    void after() {

    }

    @Before(name = "before")
    void before() {

    }

    @AfterAll
    static void afterAll() {

    }

    @BeforeAll
    static void beforeAll() {

    }

    @DocStringType
    @Nullable
    Object json(String docString) {
        return null;
    }

}
