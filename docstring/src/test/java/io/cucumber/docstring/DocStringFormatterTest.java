package io.cucumber.docstring;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class DocStringFormatterTest {

    @Test
    void should_print_docstring_with_content_type() {
        DocString docString = DocString.create("{\n" +
                "  \"key1\": \"value1\",\n" +
                "  \"key2\": \"value2\",\n" +
                "  \"another1\": \"another2\"\n" +
                "}\n",
            "application/json");

        DocStringFormatter formatter = DocStringFormatter.builder().build();
        String format = formatter.format(docString);
        assertThat(format, equalTo(
            "\"\"\"application/json\n" +
                    "{\n" +
                    "  \"key1\": \"value1\",\n" +
                    "  \"key2\": \"value2\",\n" +
                    "  \"another1\": \"another2\"\n" +
                    "}\n" +
                    "\"\"\"\n"));
    }

    @Test
    void should_print_docstring_without_content_type() {
        DocString docString = DocString.create("{\n" +
                "  \"key1\": \"value1\",\n" +
                "  \"key2\": \"value2\",\n" +
                "  \"another1\": \"another2\"\n" +
                "}\n");

        DocStringFormatter formatter = DocStringFormatter.builder().build();
        String format = formatter.format(docString);
        assertThat(format, equalTo(
            "\"\"\"\n" +
                    "{\n" +
                    "  \"key1\": \"value1\",\n" +
                    "  \"key2\": \"value2\",\n" +
                    "  \"another1\": \"another2\"\n" +
                    "}\n" +
                    "\"\"\"\n"));
    }

    @Test
    void should_print_docstring_with_indentation() {
        DocString docString = DocString.create("Hello",
            "text/plain");

        DocStringFormatter formatter = DocStringFormatter.builder().indentation("   ").build();
        String format = formatter.format(docString);
        assertThat(format, equalTo(
            "   \"\"\"text/plain\n" +
                    "   Hello\n" +
                    "   \"\"\"\n"));
    }

}
