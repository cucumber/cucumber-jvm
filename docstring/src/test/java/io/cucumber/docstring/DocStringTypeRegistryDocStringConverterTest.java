package io.cucumber.docstring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocStringTypeRegistryDocStringConverterTest {

    static final DocStringType stringForText = new DocStringType(
        String.class,
        "text",
        (String s) -> s);
    static final DocStringType stringForXml = new DocStringType(
        String.class,
        "xml",
        (String s) -> s);
    static final DocStringType stringForYaml = new DocStringType(
        String.class,
        "yml",
        (String s) -> s);
    static final DocStringType stringForJson = new DocStringType(
        String.class,
        "json",
        (String s) -> s);
    static final DocStringType jsonNodeForJson = new DocStringType(
        JsonNode.class,
        "json",
        (String s) -> new ObjectMapper().readTree(s));
    static final DocStringType jsonNodeForXml = new DocStringType(
        JsonNode.class,
        "xml",
        (String s) -> new ObjectMapper().readTree(s));
    static final DocStringType jsonNodeForJsonThrowsException = new DocStringType(
        JsonNode.class,
        "json",
        (String s) -> {
            throw new RuntimeException();
        });

    final DocStringTypeRegistry registry = new DocStringTypeRegistry();
    final DocStringTypeRegistryDocStringConverter converter = new DocStringTypeRegistryDocStringConverter(registry);

    @Test
    void doc_string_is_not_converted() {
        DocString docString = DocString.create("{\"hello\":\"world\"}");
        DocString converted = converter.convert(docString, DocString.class);
        assertThat(converted, is(docString));
    }

    @Test
    void anonymous_to_string_uses_default() {
        DocString docString = DocString.create("hello world");
        assertThat(converter.convert(docString, String.class), is("hello world"));
    }

    @Test
    void unregistered_to_string_uses_default() {
        DocString docString = DocString.create("hello world", "unregistered");
        assertThat(converter.convert(docString, String.class), is("hello world"));
    }

    @Test
    void anonymous_to_json_node_uses_registered() {
        registry.defineDocStringType(jsonNodeForJson);
        DocString docString = DocString.create("{\"hello\":\"world\"}");
        JsonNode converted = converter.convert(docString, JsonNode.class);
        assertThat(converted.get("hello").textValue(), is("world"));
    }

    @Test
    void json_to_string_with_registered_json_for_json_node_uses_default() {
        registry.defineDocStringType(jsonNodeForJson);
        DocString docString = DocString.create("hello world", "json");
        assertThat(converter.convert(docString, String.class), is("hello world"));
    }

    @Test
    void throws_when_uses_doc_string_type_but_downcast_conversion() {
        registry.defineDocStringType(jsonNodeForJson);
        DocString docString = DocString.create("{\"hello\":\"world\"}", "json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, Object.class));
        assertThat(exception.getMessage(), is("" +
                "It appears you did not register docstring type for 'json' or java.lang.Object"));
    }

    @Test
    void throws_if_converter_type_conflicts_with_type() {
        registry.defineDocStringType(jsonNodeForJson);
        registry.defineDocStringType(stringForText);
        DocString docString = DocString.create("hello world", "json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, String.class));
        assertThat(exception.getMessage(),
            is("Multiple converters found for type java.lang.String, and the content type 'json' " +
                    "did not match any of the registered types [[anonymous], text]. Change the content type of the docstring "
                    +
                    "or register a docstring type for 'json'"));
    }

    @Test
    void throws_when_no_converter_available() {
        DocString docString = DocString.create("{\"hello\":\"world\"}", "application/json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception.getMessage(), is("" +
                "It appears you did not register docstring type for 'application/json' or com.fasterxml.jackson.databind.JsonNode"));
    }

    @Test
    void throws_when_no_converter_available_for_type() {
        DocString docString = DocString.create("{\"hello\":\"world\"}");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception.getMessage(), is("" +
                "It appears you did not register docstring type for com.fasterxml.jackson.databind.JsonNode"));
    }

    @Test
    void throws_when_multiple_convertors_available() {
        registry.defineDocStringType(jsonNodeForJson);
        registry.defineDocStringType(jsonNodeForXml);
        DocString docString = DocString.create("{\"hello\":\"world\"}");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception.getMessage(), is("" +
                "Multiple converters found for type com.fasterxml.jackson.databind.JsonNode, " +
                "add one of the following content types to your docstring [json, xml]"));
    }

    @Test
    void throws_when_conversion_fails() {
        registry.defineDocStringType(jsonNodeForJsonThrowsException);
        DocString docString = DocString.create("{\"hello\":\"world\"}", "json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception.getMessage(), is(equalToCompressingWhiteSpace("" +
                "'json' could not transform\n" +
                "      \"\"\"json\n" +
                "      {\"hello\":\"world\"}\n" +
                "      \"\"\"")));
    }

    @Test
    void different_docstring_content_types_convert_to_matching_doc_string_types() {
        registry.defineDocStringType(stringForJson);
        registry.defineDocStringType(stringForXml);
        registry.defineDocStringType(stringForYaml);
        DocString docStringJson = DocString.create("{\"content\":\"hello world\"}", "json");
        DocString docStringXml = DocString.create("<content>hello world</content>}", "xml");
        DocString docStringYml = DocString.create("content: hello world", "yml");

        assertAll(
            () -> assertThat(docStringJson.getContent(), equalTo(converter.convert(docStringJson, String.class))),
            () -> assertThat(docStringXml.getContent(), equalTo(converter.convert(docStringXml, String.class))),
            () -> assertThat(docStringYml.getContent(), equalTo(converter.convert(docStringYml, String.class))));
    }

    @Test
    void same_docstring_content_type_can_convert_to_different_registered_doc_string_types() {
        registry.defineDocStringType(new DocStringType(
            Greet.class,
            "text",
            Greet::new));

        registry.defineDocStringType(new DocStringType(
            Meet.class,
            "text",
            Meet::new));

        registry.defineDocStringType(new DocStringType(
            Leave.class,
            "text",
            Leave::new));

        DocString docStringGreet = DocString.create(
            "hello world", "text");
        DocString docStringMeet = DocString.create(
            "nice to meet", "text");
        DocString docStringLeave = DocString.create(
            "goodbye", "text");

        Greet expectedGreet = new Greet(docStringGreet.getContent());
        Meet expectedMeet = new Meet(docStringMeet.getContent());
        Leave expectedLeave = new Leave(docStringLeave.getContent());

        assertThat(converter.convert(docStringGreet, Greet.class), equalTo(expectedGreet));
        assertThat(converter.convert(docStringMeet, Meet.class), equalTo(expectedMeet));
        assertThat(converter.convert(docStringLeave, Leave.class), equalTo(expectedLeave));
    }

    private static class Greet {
        private final String message;

        Greet(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Greet greet = (Greet) o;
            return Objects.equals(message, greet.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

    }

    private static class Meet {
        private final String message;

        Meet(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Meet meet = (Meet) o;
            return Objects.equals(message, meet.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

    }

    private static class Leave {
        private final String message;

        Leave(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Leave leave = (Leave) o;
            return Objects.equals(message, leave.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

    }

}
