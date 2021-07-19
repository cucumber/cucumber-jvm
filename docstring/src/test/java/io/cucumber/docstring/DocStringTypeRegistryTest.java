package io.cucumber.docstring;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocStringTypeRegistryTest {

    public static final String DEFAULT_CONTENT_TYPE = "";
    private final DocStringTypeRegistry registry = new DocStringTypeRegistry();

    @Test
    void anonymous_doc_string_is_predefined() {
        DocStringType docStringType = new DocStringType(
            String.class,
            DEFAULT_CONTENT_TYPE,
            (String s) -> s);

        CucumberDocStringException actualThrown = assertThrows(
            CucumberDocStringException.class, () -> registry.defineDocStringType(docStringType));
        assertThat(actualThrown.getMessage(), is(equalTo(
            "There is already docstring type registered for '[anonymous]' and java.lang.String.\n" +
                    "You are trying to add '[anonymous]' and java.lang.String")));
    }

    @Test
    void doc_string_types_of_same_content_type_must_have_unique_return_type() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "application/json",
            (String s) -> null));

        DocStringType duplicate = new DocStringType(
            JsonNode.class,
            "application/json",
            (String s) -> null);

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> registry.defineDocStringType(duplicate));
        assertThat(exception.getMessage(), is("" +
                "There is already docstring type registered for 'application/json' and com.fasterxml.jackson.databind.JsonNode.\n"
                +
                "You are trying to add 'application/json' and com.fasterxml.jackson.databind.JsonNode"));
    }

    @Test
    void can_register_multiple_doc_string_with_different_content_type_but_same_return_type() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "application/json",
            (String s) -> null));

        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "application/xml",
            (String s) -> null));

        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "application/yml",
            (String s) -> null));

        // default String register
        assertThat(registry.getDocStringTypes().size(), is(4));
    }

    @Test
    void no_content_type_association_is_made() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "application/json",
            (String s) -> null));

        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> null));

        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json/application",
            (String s) -> null));

        // default String register
        assertThat(registry.getDocStringTypes().size(), is(4));
    }

    @Test
    void can_add_multiple_default_content_types_with_different_return_types() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            DEFAULT_CONTENT_TYPE,
            (String s) -> null));

        registry.defineDocStringType(new DocStringType(
            TreeNode.class,
            DEFAULT_CONTENT_TYPE,
            (String s) -> null));

        // default String register
        assertThat(registry.getDocStringTypes().get(DEFAULT_CONTENT_TYPE).size(), is(3));
    }

}
