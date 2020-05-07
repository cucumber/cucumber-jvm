package io.cucumber.docstring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocStringTypeRegistryDocStringConverterTest {

    private final DocStringTypeRegistry registry = new DocStringTypeRegistry();
    private final DocStringTypeRegistryDocStringConverter converter = new DocStringTypeRegistryDocStringConverter(
        registry);

    @Test
    void uses_doc_string_type_when_available() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> new ObjectMapper().readTree(s)));

        DocString docString = DocString.create(
            "{\"hello\":\"world\"}",
            "json");

        JsonNode converted = converter.convert(docString, Object.class);
        assertThat(converted.get("hello").textValue(), is("world"));
    }

    @Test
    void uses_target_type_when_available() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> new ObjectMapper().readTree(s)));

        DocString docString = DocString.create(
            "{\"hello\":\"world\"}");

        JsonNode converted = converter.convert(docString, JsonNode.class);
        assertThat(converted.get("hello").textValue(), is("world"));
    }

    @Test
    void target_type_to_string_is_predefined() {
        DocString docString = DocString.create(
            "hello world");
        String converted = converter.convert(docString, String.class);
        assertThat(converted, is("hello world"));
    }

    @Test
    void converts_doc_string_to_doc_string() {
        DocString docString = DocString.create(
            "{\"hello\":\"world\"}");

        DocString converted = converter.convert(docString, DocString.class);
        assertThat(converted, is(docString));
    }

    @Test
    void throws_when_no_converter_available() {
        DocString docString = DocString.create(
            "{\"hello\":\"world\"}",
            "application/json");

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));

        assertThat(exception.getMessage(), is("" +
                "It appears you did not register docstring type for 'application/json' or com.fasterxml.jackson.databind.JsonNode"));
    }

    @Test
    void throws_when_no_converter_available_for_type() {
        DocString docString = DocString.create(
            "{\"hello\":\"world\"}");

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));

        assertThat(exception.getMessage(), is("" +
                "It appears you did not register docstring type for com.fasterxml.jackson.databind.JsonNode"));
    }

    @Test
    void throws_when_conversion_fails() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> {
                throw new RuntimeException();
            }));

        DocString docString = DocString.create(
            "{\"hello\":\"world\"}",
            "json");

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));

        assertThat(exception.getMessage(), is(equalToCompressingWhiteSpace("" +
                "'json' could not transform\n" +
                "      \"\"\"json\n" +
                "      {\"hello\":\"world\"}\n" +
                "      \"\"\"")));
    }

}
