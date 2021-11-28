package io.cucumber.docstring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocStringTypeRegistryDocStringConverterTest {

    private final DocStringTypeRegistry registry = new DocStringTypeRegistry();
    private final DocStringTypeRegistryDocStringConverter converter = new DocStringTypeRegistryDocStringConverter(
        registry);

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
    void converts_no_content_type_doc_string_to_registered_matching_convertor() {
        DocString docString = DocString.create(
            "{\"hello\":\"world\"}");

        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> new ObjectMapper().convertValue(s, JsonNode.class)));

        JsonNode converted = converter.convert(docString, JsonNode.class);
        assertThat(converted.asText(), equalTo(docString.getContent()));
    }

    @Test
    void throws_when_no_converter_defined() {
        DocString docString = DocString.create("hello world");

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> docString.convert(Object.class));

        assertThat(exception.getMessage(), is("" +
                "Can't convert DocString to class java.lang.Object. You have to write the conversion for it in this method"));
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

    @Test
    void throws_when_uses_doc_string_type_but_downcast_conversion() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> new ObjectMapper().readTree(s)));

        DocString docString = DocString.create(
            "{\"hello\":\"world\"}",
            "json");

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, Object.class));

        assertThat(exception.getMessage(), is("" +
                "It appears you did not register docstring type for 'json' or java.lang.Object"));
    }

    @Test
    void throws_when_multiple_convertors_available() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> new ObjectMapper().readTree(s)));

        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "xml",
            (String s) -> new ObjectMapper().readTree(s)));

        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "",
            (String s) -> new ObjectMapper().readTree(s)));

        DocString docString = DocString.create(
            "{\"hello\":\"world\"}");

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));

        assertThat(exception.getMessage(), is("" +
                "Multiple converters found for type com.fasterxml.jackson.databind.JsonNode, " +
                "add one of the following content types to docstring [json, xml]"));
    }

    @Test
    void different_docstring_content_types_convert_to_matching_doc_string_types() {
        registry.defineDocStringType(new DocStringType(
            String.class,
            "json",
            (String s) -> s));

        registry.defineDocStringType(new DocStringType(
            String.class,
            "xml",
            (String s) -> s));

        registry.defineDocStringType(new DocStringType(
            String.class,
            "yml",
            (String s) -> s));

        DocString docStringJson = DocString.create(
            "{\"content\":\"hello world\"}", "json");
        DocString docStringXml = DocString.create(
            "<content>hello world</content>}", "xml");
        DocString docStringYml = DocString.create(
            "  content: hello world", "yml");

        String convertJson = converter.convert(docStringJson, String.class);
        String convertXml = converter.convert(docStringXml, String.class);
        String convertYml = converter.convert(docStringYml, String.class);

        assertThat(docStringJson.getContent(), equalTo(convertJson));
        assertThat(docStringXml.getContent(), equalTo(convertXml));
        assertThat(docStringYml.getContent(), equalTo(convertYml));
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

        Greet actualGreet = converter.convert(docStringGreet, Greet.class);
        Meet actualMeet = converter.convert(docStringMeet, Meet.class);
        Leave actualLeave = converter.convert(docStringLeave, Leave.class);

        Greet expectedGreet = new Greet(docStringGreet.getContent());
        Meet expectedMeet = new Meet(docStringMeet.getContent());
        Leave expectedLeave = new Leave(docStringLeave.getContent());

        assertThat(actualGreet, equalTo(expectedGreet));
        assertThat(actualMeet, equalTo(expectedMeet));
        assertThat(actualLeave, equalTo(expectedLeave));
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
