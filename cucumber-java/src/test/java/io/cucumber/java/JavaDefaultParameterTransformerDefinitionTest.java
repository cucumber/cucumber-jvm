package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaDefaultParameterTransformerDefinitionTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaDefaultParameterTransformerDefinitionTest.this;
        }
    };

    @Test
    void can_transform_string_to_type() throws Throwable {
        Method method = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("transform_string_to_type",
            String.class, Type.class);
        JavaDefaultParameterTransformerDefinition definition = new JavaDefaultParameterTransformerDefinition(method,
            lookup);
        Object transformed = definition.parameterByTypeTransformer().transform("something", String.class);
        assertThat(transformed, is("transform_string_to_type"));
    }

    public Object transform_string_to_type(String fromValue, Type toValueType) {
        return "transform_string_to_type";
    }

    @Test
    void can_transform_object_to_type() throws Throwable {
        Method method = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("transform_object_to_type",
            Object.class, Type.class);
        JavaDefaultParameterTransformerDefinition definition = new JavaDefaultParameterTransformerDefinition(method,
            lookup);
        String transformed = (String) definition.parameterByTypeTransformer().transform("something", String.class);
        assertThat(transformed, is("transform_object_to_type"));
    }

    public Object transform_object_to_type(Object fromValue, Type toValueType) {
        return "transform_object_to_type";
    }

    @Test
    void must_have_non_void_return() throws Throwable {
        Method method = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("transforms_string_to_void",
            String.class, Type.class);
        InvalidMethodSignatureException exception = assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(method, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A @DefaultParameterTransformer annotated method must have one of these signatures:\n" +
                " * public Object defaultDataTableEntry(String fromValue, Type toValueType)\n" +
                " * public Object defaultDataTableEntry(Object fromValue, Type toValueType)\n" +
                "at io.cucumber.java.JavaDefaultParameterTransformerDefinitionTest.transforms_string_to_void(java.lang.String,java.lang.reflect.Type)"));
    }

    public void transforms_string_to_void(String fromValue, Type toValueType) {
    }

    @Test
    void must_have_two_arguments() throws Throwable {
        Method oneArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("one_argument", String.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(oneArg, lookup));
        Method threeArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("three_arguments", String.class,
            Type.class, Object.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(threeArg, lookup));
    }

    public Object one_argument(String fromValue) {
        return "one_arguments";
    }

    public Object three_arguments(String fromValue, Type toValueType, Object extra) {
        return "three_arguments";
    }

    @Test
    void must_have_string_or_object_as_from_value() throws Throwable {
        Method threeArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("map_as_from_value", Map.class,
            Type.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(threeArg, lookup));
    }

    public Object map_as_from_value(Map<String, String> fromValue, Type toValueType) {
        return "map_as_from_value";
    }

    @Test
    void must_have_type_as_to_value_type() throws Throwable {
        Method threeArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("object_as_to_value_type",
            String.class, Object.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(threeArg, lookup));
    }

    public Object object_as_to_value_type(String fromValue, Object toValueType) {
        return "object_as_to_value_type";
    }

}
