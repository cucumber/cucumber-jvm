package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.TableCellByTypeTransformer;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JavaDefaultDataTableEntryTransformerDefinitionTest {

    private final Map<String, String> fromValue = singletonMap("key", "value");
    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaDefaultDataTableEntryTransformerDefinitionTest.this;
        }
    };

    private TableCellByTypeTransformer cellTransformer = new TableCellByTypeTransformer() {
        @Override
        public <T> T transform(String value, Class<T> cellType) {
            throw new IllegalStateException();
        }
    };

    @Test
    public void transforms_with_correct_method() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("correct_method", Map.class, Class.class);
        JavaDefaultDataTableEntryTransformerDefinition definition =
            new JavaDefaultDataTableEntryTransformerDefinition(method, lookup);

        assertThat(definition.tableEntryByTypeTransformer()
            .transform(fromValue, String.class, cellTransformer), is("key=value"));

    }

    public <T> T correct_method(Map<String, String> fromValue, Class<T> toValue) {
        return join(fromValue);
    }


    @Test
    public void transforms_with_correct_method_with_cell_transformer() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("correct_method_with_cell_transformer", Map.class, Class.class, TableCellByTypeTransformer.class);
        JavaDefaultDataTableEntryTransformerDefinition definition =
            new JavaDefaultDataTableEntryTransformerDefinition(method, lookup);

        assertThat(definition.tableEntryByTypeTransformer()
            .transform(fromValue, String.class, cellTransformer), is("key=value"));

    }


    public <T> T correct_method_with_cell_transformer(Map<String, String> fromValue, Class<T> toValue, TableCellByTypeTransformer cellTransformer) {
        return join(fromValue);
    }

    @Test
    public void method_must_have_2_or_3_arguments() throws Throwable {
        Method toFew = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("one_argument", Map.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(toFew, lookup));
        Method toMany = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("four_arguments", Map.class, String.class, String.class, String.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(toMany, lookup));
    }

    public <T> T one_argument(Map<String, String> fromValue) {
        return null;
    }


    public <T> T four_arguments(Map<String, String> fromValue, String one, String two, String three) {
        return null;
    }


    @Test
    public void method_must_have_return_type() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("void_return_type", Map.class, Class.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
    }

    public void void_return_type(Map<String, String> fromValue, Class<?> toValue) {
    }


    @Test
    public void method_must_have_map_as_first_argument() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_first_type", String.class, Class.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
        Method method2 = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_first_type", List.class, Class.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(method2, lookup));
        Method method3 = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_first_type", Map.class, Class.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(method3, lookup));
    }


    public <T> T invalid_first_type(String fromValue, Class<T> toValue) {
        return null;
    }

    public <T> T invalid_first_type(List<String> fromValue, Class<T> toValue) {
        return null;
    }

    public <T> T invalid_first_type(Map<String, Object> fromValue, Class<T> toValue) {
        return null;
    }

    @Test
    public void method_must_have_class_as_second_argument() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_second_type", Map.class, String.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
    }

    public <T> T invalid_second_type(Map<String, String> fromValue, String toValue) {
        return null;
    }


    @Test
    public void method_must_have_cell_transformer_as_optional_third_argument() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_optional_third_type", Map.class, Class.class, String.class);
        assertThrows(CucumberException.class, () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
    }


    public <T> T invalid_optional_third_type(Map<String, String> fromValue, Class<T> toValue, String cellTransformer) {
        return null;
    }

    private static <T> T join(Map<String, String> fromValue) {
        //noinspection unchecked
        return (T) fromValue.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining());
    }


}