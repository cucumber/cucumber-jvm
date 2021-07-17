package io.cucumber.docstring;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DocStringTest {

    @Test
    void pretty_prints_doc_string_objects() {
        DocString docString = DocString.create(
            "{\n" +
                    "  \"hello\":\"world\"\n" +
                    "}",
            "application/json");

        assertThat(docString.toString(), is("" +
                "      \"\"\"application/json\n" +
                "      {\n" +
                "        \"hello\":\"world\"\n" +
                "      }\n" +
                "      \"\"\""));
    }

    @Test
    void doc_string_equals_doc_string() {
        DocString docString = DocString.create(
            "{\n" +
                    "  \"hello\":\"world\"\n" +
                    "}",
            "application/json");

        DocString other = DocString.create(
            "{\n" +
                    "  \"hello\":\"world\"\n" +
                    "}",
            "application/json");

        assertThat(docString, CoreMatchers.equalTo(other));
        assertThat(docString.hashCode(), CoreMatchers.equalTo(other.hashCode()));
    }

    @Test
    void pretty_prints_empty_doc_string_objects() {
        DocString docString = DocString.create(
            "",
            "application/json");

        assertThat(docString.toString(), is("" +
                "      \"\"\"application/json\n" +
                "      \n" +
                "      \"\"\""));
    }

}
