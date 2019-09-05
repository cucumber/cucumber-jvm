package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.datatable.DataTable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaDataTableTypeDefinitionTest {

    private final Lookup lookup = new Lookup() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaDataTableTypeDefinitionTest.this;
        }
    };

    private final DataTable dataTable = DataTable.create(asList(
        asList("a", "b"),
        asList("c", "d")
    ));

    @Test
    void can_define_data_table_converter() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("convert_data_table_to_string", DataTable.class);
        JavaDataTableTypeDefinition definition = new JavaDataTableTypeDefinition(method, lookup);
        assertThat(definition.dataTableType().transform(dataTable.asLists()), is("convert_data_table_to_string"));
    }

    public String convert_data_table_to_string(DataTable table) {
        return "convert_data_table_to_string";
    }

    @Test
    void can_define_table_row_transformer() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("convert_table_row_to_string", List.class);
        JavaDataTableTypeDefinition definition = new JavaDataTableTypeDefinition(method, lookup);
        assertThat(definition.dataTableType().transform(dataTable.asLists()),
            is(asList("convert_table_row_to_string", "convert_table_row_to_string")));
    }

    public String convert_table_row_to_string(List<String> row) {
        return "convert_table_row_to_string";
    }

    @Test
    void can_define_table_entry_transformer() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("converts_table_entry_to_string", Map.class);
        JavaDataTableTypeDefinition definition = new JavaDataTableTypeDefinition(method, lookup);
        assertThat(definition.dataTableType().transform(dataTable.asLists()),
            is(singletonList("converts_table_entry_to_string")));
    }

    public String converts_table_entry_to_string(Map<String, String> entry) {
        return "converts_table_entry_to_string";
    }

    @Test
    void can_define_table_cell_transformer() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("converts_table_cell_to_string", String.class);
        JavaDataTableTypeDefinition definition = new JavaDataTableTypeDefinition(method, lookup);
        assertThat(definition.dataTableType().transform(dataTable.asLists()), is(asList(
            asList("converts_table_cell_to_string", "converts_table_cell_to_string"),
            asList("converts_table_cell_to_string", "converts_table_cell_to_string"))
        ));
    }

    public String converts_table_cell_to_string(String cell) {
        return "converts_table_cell_to_string";
    }

    @Test
    void target_type_must_class_type() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("converts_datatable_to_optional_string", DataTable.class);
        JavaDataTableTypeDefinition definition = new JavaDataTableTypeDefinition(method, lookup);
        assertThat(definition.dataTableType().transform(dataTable.asLists()), is(Optional.of("converts_datatable_to_optional_string")));

    }

    public Optional<String> converts_datatable_to_optional_string(DataTable table) {
        return Optional.of("converts_datatable_to_optional_string");
    }

    @Test
    void target_type_must_not_be_void() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("converts_data_table_to_void", DataTable.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDataTableTypeDefinition(method, lookup));
    }

    public void converts_data_table_to_void(DataTable table) {
    }

    @Test
    void must_have_exactly_one_argument() throws NoSuchMethodException {
        Method noArgs = JavaDataTableTypeDefinitionTest.class.getMethod("converts_nothing_to_string");
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDataTableTypeDefinition(noArgs, lookup));
        Method twoArgs = JavaDataTableTypeDefinitionTest.class.getMethod("converts_two_strings_to_string", String.class, String.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDataTableTypeDefinition(twoArgs, lookup));
    }

    public String converts_nothing_to_string() {
        return "converts_nothing_to_string";
    }

    public String converts_two_strings_to_string(String arg1, String arg2) {
        return "converts_two_strings_to_string";
    }

    @Test
    void argument_must_match_existing_transformer() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("converts_object_to_string", Object.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDataTableTypeDefinition(method, lookup));
    }

    public String converts_object_to_string(Object string) {
        return "converts_object_to_string";
    }

    @Test
    void table_entry_transformer_must_have_map_of_strings() throws NoSuchMethodException {
        Method method = JavaDataTableTypeDefinitionTest.class.getMethod("converts_map_of_objects_to_string", Map.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDataTableTypeDefinition(method, lookup));
    }

    public String converts_map_of_objects_to_string(Map<Object, Object> entry) {
        return "converts_map_of_objects_to_string";
    }

}
