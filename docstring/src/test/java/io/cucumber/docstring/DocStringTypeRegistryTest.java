package io.cucumber.docstring;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocStringTypeRegistryTest {

    private final DocStringTypeRegistry registry = new DocStringTypeRegistry();

    @Test
    void anonymous_doc_string_is_predefined() {
        DocStringType docStringType = new DocStringType(
            String.class,
            "",
            (String s) -> s);

        CucumberDocStringException actualThrown = assertThrows(
            CucumberDocStringException.class, () -> registry.defineDocStringType(docStringType));
        assertThat(actualThrown.getMessage(), is(equalTo(
            "There is already docstring type registered for '[anonymous]' and java.lang.String.\n" +
                    "You are trying to add '[anonymous]' and java.lang.String")));
    }

    @Test
    void doc_string_types_must_be_unique() {
        registry.defineDocStringType(new DocStringType(
            JsonNode.class,
            "json",
            (String s) -> null));

        DocStringType duplicate = new DocStringType(
            JsonNode.class,
            "application/json",
            (String s) -> null);

        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> registry.defineDocStringType(duplicate));
        assertThat(exception.getMessage(), is("" +
                "There is already docstring type registered for 'json' and com.fasterxml.jackson.databind.JsonNode.\n" +
                "You are trying to add 'application/json' and com.fasterxml.jackson.databind.JsonNode"));
    }

}
