package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.datatable.TableCellByTypeTransformer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaDefaultDataTableEntryTransformerDefinitionTest {

    private final Map<String, String> fromValue = singletonMap("key", "value");
    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaDefaultDataTableEntryTransformerDefinitionTest.this;
        }
    };

    private final TableCellByTypeTransformer cellTransformer = (value, cellType) -> {
        throw new IllegalStateException();
    };

    @Test
    void transforms_with_correct_method() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("correct_method", Map.class,
            Type.class);
        JavaDefaultDataTableEntryTransformerDefinition definition = new JavaDefaultDataTableEntryTransformerDefinition(
            method, lookup);

        assertThat(definition.tableEntryByTypeTransformer()
                .transform(fromValue, String.class, cellTransformer),
            is("key=value"));
    }

    @Test
    void transforms_empties_with_correct_method() throws Throwable {
        Map<String, String> fromValue = singletonMap("key", "[empty]");
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("correct_method", Map.class,
            Type.class);
        JavaDefaultDataTableEntryTransformerDefinition definition = new JavaDefaultDataTableEntryTransformerDefinition(
            method, lookup, false, new String[] { "[empty]" });

        assertThat(definition.tableEntryByTypeTransformer()
                .transform(fromValue, String.class, cellTransformer),
            is("key="));
    }

    @Test
    void transforms_nulls_with_correct_method() throws Throwable {
        Map<String, String> fromValue = singletonMap("key", null);
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("correct_method", Map.class,
            Type.class);
        JavaDefaultDataTableEntryTransformerDefinition definition = new JavaDefaultDataTableEntryTransformerDefinition(
            method, lookup, false, new String[] { "[empty]" });

        assertThat(definition.tableEntryByTypeTransformer()
                .transform(fromValue, String.class, cellTransformer),
            is("key=null"));
    }

    @Test
    void throws_for_multiple_empties_with_correct_method() throws Throwable {
        Map<String, String> fromValue = new LinkedHashMap<>();
        fromValue.put("[empty]", "a");
        fromValue.put("[blank]", "b");
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("correct_method", Map.class,
            Type.class);
        JavaDefaultDataTableEntryTransformerDefinition definition = new JavaDefaultDataTableEntryTransformerDefinition(
            method, lookup, false, new String[] { "[empty]", "[blank]" });

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> definition.tableEntryByTypeTransformer().transform(fromValue, String.class, cellTransformer));

        assertThat(exception.getMessage(), is(
            "After replacing [empty] and [blank] with empty strings the datatable entry contains duplicate keys: {[empty]=a, [blank]=b}"));
    }

    public <T> T correct_method(Map<String, String> fromValue, Type toValueType) {
        return join(fromValue);
    }

    @SuppressWarnings("unchecked")
    private static <T> T join(Map<String, String> fromValue) {
        return (T) fromValue.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining());
    }

    @Test
    void transforms_with_correct_method_with_cell_transformer() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod(
            "correct_method_with_cell_transformer", Map.class, Type.class, TableCellByTypeTransformer.class);
        JavaDefaultDataTableEntryTransformerDefinition definition = new JavaDefaultDataTableEntryTransformerDefinition(
            method, lookup);

        assertThat(definition.tableEntryByTypeTransformer()
                .transform(fromValue, String.class, cellTransformer),
            is("key=value"));
    }

    public <T> T correct_method_with_cell_transformer(
            Map<String, String> fromValue, Type toValueType, TableCellByTypeTransformer cellTransformer
    ) {
        return join(fromValue);
    }

    @Test
    void method_must_have_2_or_3_arguments() throws Throwable {
        Method toFew = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("one_argument", Map.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(toFew, lookup));
        Method toMany = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("four_arguments", Map.class,
            String.class, String.class, String.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(toMany, lookup));
    }

    public <T> T one_argument(Map<String, String> fromValue) {
        return null;
    }

    public Object four_arguments(Map<String, String> fromValue, String one, String two, String three) {
        return null;
    }

    @Test
    void method_must_have_return_type() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("void_return_type",
            Map.class, Type.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
    }

    public void void_return_type(Map<String, String> fromValue, Type toValueType) {
    }

    @Test
    void method_must_have_map_as_first_argument() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_first_type",
            String.class, Type.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
        Method method2 = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_first_type",
            List.class, Type.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(method2, lookup));
        Method method3 = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_first_type",
            Map.class, Type.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(method3, lookup));
    }

    public Object invalid_first_type(String fromValue, Type toValueType) {
        return null;
    }

    public Object invalid_first_type(List<String> fromValue, Type toValueType) {
        return null;
    }

    public Object invalid_first_type(Map<String, Object> fromValue, Type toValueType) {
        return null;
    }

    @Test
    void method_must_have_class_as_second_argument() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class.getMethod("invalid_second_type",
            Map.class, String.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
    }

    public Object invalid_second_type(Map<String, String> fromValue, String toValue) {
        return null;
    }

    @Test
    void method_must_have_cell_transformer_as_optional_third_argument() throws Throwable {
        Method method = JavaDefaultDataTableEntryTransformerDefinitionTest.class
                .getMethod("invalid_optional_third_type", Map.class, Type.class, String.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultDataTableEntryTransformerDefinition(method, lookup));
    }

    public Object invalid_optional_third_type(Map<String, String> fromValue, Type toValueType, String cellTransformer) {
        return null;
    }

}
