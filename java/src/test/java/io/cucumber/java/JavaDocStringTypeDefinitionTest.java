package io.cucumber.java;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.core.backend.Lookup;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringTypeRegistry;
import io.cucumber.docstring.DocStringTypeRegistryDocStringConverter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaDocStringTypeDefinitionTest {

    private final Lookup lookup = new Lookup() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaDocStringTypeDefinitionTest.this;
        }
    };

    private final DocString docString = DocString.create("some doc string", "text/plain");
    private final DocStringTypeRegistry registry = new DocStringTypeRegistry();
    private final DocStringTypeRegistryDocStringConverter converter = new DocStringTypeRegistryDocStringConverter(
        registry);

    @Test
    void can_define_doc_string_converter() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("convert_doc_string_to_string", String.class);
        JavaDocStringTypeDefinition definition = new JavaDocStringTypeDefinition("text/plain", method, lookup);
        registry.defineDocStringType(definition.docStringType());
        assertThat(converter.convert(docString, Object.class), is("some_desired_string"));
    }

    @Test
    void can_define_doc_string_without_content_types_converter() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("convert_doc_string_to_string", String.class);
        JavaDocStringTypeDefinition definition = new JavaDocStringTypeDefinition("", method, lookup);
        registry.defineDocStringType(definition.docStringType());
        assertThat(converter.convert(DocString.create("some doc string"), Object.class),
            is("some_desired_string"));
    }

    public Object convert_doc_string_to_string(String docString) {
        return "some_desired_string";
    }

    @Test
    void must_have_exactly_one_argument() throws NoSuchMethodException {
        Method noArgs = JavaDocStringTypeDefinitionTest.class.getMethod("converts_nothing_to_string");
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDocStringTypeDefinition("", noArgs, lookup));
        Method twoArgs = JavaDocStringTypeDefinitionTest.class.getMethod("converts_two_strings_to_string", String.class,
            String.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDocStringTypeDefinition("", twoArgs, lookup));
    }

    public Object converts_nothing_to_string() {
        return "converts_nothing_to_string";
    }

    public Object converts_two_strings_to_string(String arg1, String arg2) {
        return "converts_two_strings_to_string";
    }

    @Test
    void must_have_exactly_string_argument() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("converts_object_to_string", Object.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaDocStringTypeDefinition("", method, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A @DocStringType annotated method must have one of these signatures:\n" +
                " * public JsonNode json(String content)\n" +
                "at io.cucumber.java.JavaDocStringTypeDefinitionTest.converts_object_to_string(java.lang.Object)"));
    }

    public Object converts_object_to_string(Object object) {
        return "converts_object_to_string";
    }

    @Test
    void must_return_something() throws NoSuchMethodException {
        Method voidMethod = JavaDocStringTypeDefinitionTest.class.getMethod("converts_string_to_void", String.class);
        Method voidObjectMethod = JavaDocStringTypeDefinitionTest.class.getMethod("converts_string_to_void_object", String.class);

        assertAll(
                () -> assertThrows(InvalidMethodSignatureException.class,
                        () -> new JavaDocStringTypeDefinition("", voidMethod, lookup)),
                () -> assertThrows(InvalidMethodSignatureException.class,
                        () -> new JavaDocStringTypeDefinition("", voidObjectMethod, lookup))
        );
    }

    public void converts_string_to_void(String docString) {
    }

    public Void converts_string_to_void_object(String docString) {
        return null;
    }

    @Test
    public void correct_conversion_is_used_for_simple_and_complex_return_types() throws NoSuchMethodException {
        Method simpleMethod = JavaDocStringTypeDefinitionTest.class.getMethod("converts_string_to_simple_type",
            String.class);
        JavaDocStringTypeDefinition simpleDefinition = new JavaDocStringTypeDefinition("text/plain", simpleMethod,
            lookup);
        registry.defineDocStringType(simpleDefinition.docStringType());

        Method complexMethod = JavaDocStringTypeDefinitionTest.class.getMethod("converts_string_to_complex_type",
            String.class);
        JavaDocStringTypeDefinition complexDefinition = new JavaDocStringTypeDefinition("text/plain", complexMethod,
            lookup);
        registry.defineDocStringType(complexDefinition.docStringType());

        Type simpleType = Map.class;
        assertThat(converter.convert(docString, simpleType), hasEntry("some_simple_type", Collections.emptyMap()));
        Type complexType = new TypeReference<Map<String, Map<String, String>>>() {
        }.getType();
        assertThat(converter.convert(docString, complexType), hasEntry("some_complex_type", Collections.emptyMap()));
    }

    @SuppressWarnings("rawtypes")
    public Map converts_string_to_simple_type(String docString) {
        return Collections.singletonMap("some_simple_type", Collections.emptyMap());
    }

    public Map<String, Map<String, String>> converts_string_to_complex_type(String docString) {
        return Collections.singletonMap("some_complex_type", Collections.emptyMap());
    }

}
