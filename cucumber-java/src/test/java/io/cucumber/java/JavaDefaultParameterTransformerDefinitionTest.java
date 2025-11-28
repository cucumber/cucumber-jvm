package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;
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

    @Test
    void can_transform_string_to_type_ignoring_locale() throws Throwable {
        Method method = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("transform_string_to_type",
            String.class, Type.class);
        JavaDefaultParameterTransformerDefinition definition = new JavaDefaultParameterTransformerDefinition(method,
            lookup);
        Object transformed = definition.parameterByTypeTransformer(Locale.ENGLISH).transform("something", String.class);
        assertThat(transformed, is("transform_string_to_type"));
    }

    public Object transform_string_to_type(String fromValue, Type toValueType) {
        return "transform_string_to_type";
    }

    @Test
    void can_transform_string_to_type_using_locale() throws Throwable {
        Method method = JavaDefaultParameterTransformerDefinitionTest.class.getMethod(
            "transform_string_to_type_with_locale", String.class, Type.class, Locale.class);
        JavaDefaultParameterTransformerDefinition definition = new JavaDefaultParameterTransformerDefinition(method,
            lookup);
        Object transformed = definition.parameterByTypeTransformer(Locale.ENGLISH).transform("something", String.class);
        assertThat(transformed, is("transform_string_to_type_with_locale_en"));
    }

    public Object transform_string_to_type_with_locale(String fromValue, Type toValueType, Locale locale) {
        return "transform_string_to_type_with_locale_" + locale.getLanguage();
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

    @Test
    void can_transform_object_to_type_ignoring_locale() throws Throwable {
        Method method = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("transform_object_to_type",
            Object.class, Type.class);
        JavaDefaultParameterTransformerDefinition definition = new JavaDefaultParameterTransformerDefinition(method,
            lookup);
        String transformed = (String) definition.parameterByTypeTransformer(Locale.ENGLISH).transform("something",
            String.class);
        assertThat(transformed, is("transform_object_to_type"));
    }

    public Object transform_object_to_type(Object fromValue, Type toValueType) {
        return "transform_object_to_type";
    }

    @Test
    void can_transform_object_to_type_using_locale() throws Throwable {
        Method method = JavaDefaultParameterTransformerDefinitionTest.class.getMethod(
            "transform_object_to_type_with_locale", Object.class, Type.class, Locale.class);
        JavaDefaultParameterTransformerDefinition definition = new JavaDefaultParameterTransformerDefinition(method,
            lookup);
        String transformed = (String) definition.parameterByTypeTransformer(Locale.ENGLISH).transform("something",
            String.class);
        assertThat(transformed, is("transform_object_to_type_with_locale_en"));
    }

    public Object transform_object_to_type_with_locale(Object fromValue, Type toValueType, Locale locale) {
        return "transform_object_to_type_with_locale_" + locale.getLanguage();
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
                " * public Object defaultDataTableEntry(Object fromValue, Type toValueType, Locale locale)\n" +
                " * public Object defaultDataTableEntry(Object fromValue, Type toValueType, Locale locale)\n" +
                "at io.cucumber.java.JavaDefaultParameterTransformerDefinitionTest.transforms_string_to_void(java.lang.String,java.lang.reflect.Type)"));
    }

    public void transforms_string_to_void(String fromValue, Type toValueType) {
    }

    @Test
    void must_have_two_or_three_arguments() throws Throwable {
        Method oneArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("one_argument", String.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(oneArg, lookup));
        Method fourArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("four_arguments", String.class,
            Type.class, Object.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(fourArg, lookup));
    }

    public Object one_argument(String fromValue) {
        return "one_argument";
    }

    public Object four_arguments(String fromValue, Type toValueType, Locale locale, Object extra) {
        return "four_arguments";
    }

    @Test
    void must_have_string_or_object_as_from_value() throws Throwable {
        Method twoArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("map_as_from_value", Map.class,
            Type.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(twoArg, lookup));
        Method threeArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("map_as_from_value_with_locale",
            Map.class, Type.class, Locale.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(threeArg, lookup));
    }

    public Object map_as_from_value(Map<String, String> fromValue, Type toValueType) {
        return "map_as_from_value";
    }

    public Object map_as_from_value_with_locale(Map<String, String> fromValue, Type toValueType, Locale locale) {
        return "map_as_from_value_with_locale";
    }

    @Test
    void must_have_type_as_to_value_type() throws Throwable {
        Method twoArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod("object_as_to_value_type",
            String.class, Object.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(twoArg, lookup));
        Method threeArg = JavaDefaultParameterTransformerDefinitionTest.class.getMethod(
            "object_as_to_value_type_with_locale", String.class, Object.class, Locale.class);
        assertThrows(InvalidMethodSignatureException.class,
            () -> new JavaDefaultParameterTransformerDefinition(threeArg, lookup));
    }

    public Object object_as_to_value_type(String fromValue, Object toValueType) {
        return "object_as_to_value_type";
    }

    public Object object_as_to_value_type_with_locale(String fromValue, Object toValueType, Locale locale) {
        return "object_as_to_value_type_with_locale";
    }

}
