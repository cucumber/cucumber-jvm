package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringTypeRegistry;
import io.cucumber.docstring.DocStringTypeRegistryDocStringConverter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
        JavaDocStringTypeDefinition definition = new JavaDocStringTypeDefinition("", method, lookup);
        registry.defineDocStringType(definition.docStringType());
        assertThat(converter.convert(docString, Object.class), is("convert_doc_string_to_string"));
    }

    public Object convert_doc_string_to_string(String docString) {
        return "convert_doc_string_to_string";
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
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("converts_string_to_void", String.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDocStringTypeDefinition("", method, lookup));
    }

    public void converts_string_to_void(String docString) {
    }

}
