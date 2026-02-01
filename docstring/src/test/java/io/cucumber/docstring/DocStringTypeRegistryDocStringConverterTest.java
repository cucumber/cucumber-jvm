package io.cucumber.docstring;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(converted).isEqualTo(docString);
    }

    @Test
    void anonymous_to_string_uses_default() {
        DocString docString = DocString.create("hello world");
        String converted = converter.convert(docString, String.class);
        assertThat(converted).isEqualTo("hello world");
    }

    @Test
    void unregistered_to_string_uses_default() {
        DocString docString = DocString.create("hello world", "unregistered");
        String converted = converter.convert(docString, String.class);
        assertThat(converted).isEqualTo("hello world");
    }

    @Test
    void anonymous_to_json_node_uses_registered() {
        registry.defineDocStringType(jsonNodeForJson);
        DocString docString = DocString.create("{\"hello\":\"world\"}");
        TreeNode converted = converter.convert(docString, JsonNode.class);
        assertThat(converted)
                .extracting(jsonNode -> jsonNode.get("hello"))
                .extracting(JsonNode.class::cast)
                .extracting(JsonNode::textValue)
                .isEqualTo("world");
    }

    @Test
    void json_to_string_with_registered_json_for_json_node_uses_default() {
        registry.defineDocStringType(jsonNodeForJson);
        DocString docString = DocString.create("hello world", "json");
        String converted = converter.convert(docString, String.class);
        assertThat(converted).isEqualTo("hello world");
    }

    @Test
    void throws_when_uses_doc_string_type_but_downcast_conversion() {
        registry.defineDocStringType(jsonNodeForJson);
        DocString docString = DocString.create("{\"hello\":\"world\"}", "json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, Object.class));
        assertThat(exception)
                .hasMessage("It appears you did not register docstring type for 'json' or java.lang.Object");
    }

    @Test
    void throws_if_converter_type_conflicts_with_type() {
        registry.defineDocStringType(jsonNodeForJson);
        registry.defineDocStringType(stringForText);
        DocString docString = DocString.create("hello world", "json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, String.class));
        assertThat(exception).hasMessage(
            "Multiple converters found for type java.lang.String, and the content type 'json' " +
                    "did not match any of the registered types [[anonymous], text]. Change the content type of the docstring "
                    +
                    "or register a docstring type for 'json'");
    }

    @Test
    void throws_when_no_converter_available() {
        DocString docString = DocString.create("{\"hello\":\"world\"}", "application/json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception).hasMessage(
            "It appears you did not register docstring type for 'application/json' or com.fasterxml.jackson.databind.JsonNode");
    }

    @Test
    void throws_when_no_converter_available_for_type() {
        DocString docString = DocString.create("{\"hello\":\"world\"}");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception).hasMessage(
            "It appears you did not register docstring type for com.fasterxml.jackson.databind.JsonNode");
    }

    @Test
    void throws_when_multiple_convertors_available() {
        registry.defineDocStringType(jsonNodeForJson);
        registry.defineDocStringType(jsonNodeForXml);
        DocString docString = DocString.create("{\"hello\":\"world\"}");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception).hasMessage(
            "Multiple converters found for type com.fasterxml.jackson.databind.JsonNode, " +
                    "add one of the following content types to your docstring [json, xml]");
    }

    @Test
    void throws_when_conversion_fails() {
        registry.defineDocStringType(jsonNodeForJsonThrowsException);
        DocString docString = DocString.create("{\"hello\":\"world\"}", "json");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> converter.convert(docString, JsonNode.class));
        assertThat(exception.getMessage()).isEqualToNormalizingNewlines("""
                'json' could not transform
                ""\"json
                {"hello":"world"}
                ""\"
                """);
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
            () -> assertThat(docStringJson.getContent()).isEqualTo(converter.convert(docStringJson, String.class)),
            () -> assertThat(docStringXml.getContent()).isEqualTo(converter.convert(docStringXml, String.class)),
            () -> assertThat(docStringYml.getContent()).isEqualTo(converter.convert(docStringYml, String.class)));
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

        assertThat((Greet) converter.convert(docStringGreet, Greet.class)).isEqualTo(expectedGreet);
        assertThat((Meet) converter.convert(docStringMeet, Meet.class)).isEqualTo(expectedMeet);
        assertThat((Leave) converter.convert(docStringLeave, Leave.class)).isEqualTo(expectedLeave);
    }

    private record Greet(String message) {

    }

    private record Meet(String message) {

    }

    private record Leave(String message) {
    }

}
