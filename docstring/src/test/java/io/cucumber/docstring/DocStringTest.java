package io.cucumber.docstring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocStringTest {

    @Test
    void throws_when_no_converter_defined() {
        DocString docString = DocString.create("hello world");
        CucumberDocStringException exception = assertThrows(
            CucumberDocStringException.class,
            () -> docString.convert(Object.class));
        assertThat(exception).hasMessage(
            "Can't convert DocString to class java.lang.Object. You have to write the conversion for it in this method");
    }

    @Test
    void pretty_prints_doc_string_objects() {
        DocString docString = DocString.create(
            """
                    {
                      "hello":"world"
                    }""",
            "application/json");

        assertThat(docString.toString()).isEqualTo("""
                ""\"application/json
                {
                  "hello":"world"
                }
                ""\"
                """);
    }

    @Test
    void doc_string_equals_doc_string() {
        DocString docString = DocString.create(
            """
                    {
                      "hello":"world"
                    }""",
            "application/json");

        DocString other = DocString.create(
            """
                    {
                      "hello":"world"
                    }""",
            "application/json");

        assertThat(docString).isEqualTo(other);
        assertThat(docString.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    void pretty_prints_empty_doc_string_objects() {
        DocString docString = DocString.create(
            "",
            "application/json");

        assertThat(docString.toString()).isEqualTo("""
                ""\"application/json

                ""\"
                """);
    }

}
