package io.cucumber.docstring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocStringFormatterTest {

    @Test
    void should_print_docstring_with_content_type() {
        DocString docString = DocString.create("""
                {
                  "key1": "value1",
                  "key2": "value2",
                  "another1": "another2"
                }
                """,
            "application/json");

        DocStringFormatter formatter = DocStringFormatter.builder().build();
        String format = formatter.format(docString);
        assertThat(format).isEqualTo(
            """
                    ""\"application/json
                    {
                      "key1": "value1",
                      "key2": "value2",
                      "another1": "another2"
                    }
                    ""\"
                    """);
    }

    @Test
    void should_print_docstring_without_content_type() {
        DocString docString = DocString.create("""
                {
                  "key1": "value1",
                  "key2": "value2",
                  "another1": "another2"
                }
                """);

        DocStringFormatter formatter = DocStringFormatter.builder().build();
        String format = formatter.format(docString);
        assertThat(format).isEqualTo(
            """
                    ""\"
                    {
                      "key1": "value1",
                      "key2": "value2",
                      "another1": "another2"
                    }
                    ""\"
                    """);
    }

    @Test
    void should_print_docstring_with_indentation() {
        DocString docString = DocString.create("Hello",
            "text/plain");

        DocStringFormatter formatter = DocStringFormatter.builder().indentation("   ").build();
        String format = formatter.format(docString);
        assertThat(format).isEqualTo(
            """
                       ""\"text/plain
                       Hello
                       ""\"
                    """);
    }

}
