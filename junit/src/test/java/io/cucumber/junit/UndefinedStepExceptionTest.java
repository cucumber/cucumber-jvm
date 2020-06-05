package io.cucumber.junit;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UndefinedStepExceptionTest {

    @Test
    void should_generate_a_message_for_a_single_snippet() {
        UndefinedStepException exception = new UndefinedStepException(singletonList("snippet"));
        assertThat(exception.getMessage(), is("" +
                "This step is undefined. You can implement it using the snippet(s) below:\n" +
                "\n" +
                "snippet\n"));
    }

    @Test
    void should_generate_a_message_for_step_without_additional_snippets() {
        UndefinedStepException exception = new UndefinedStepException(
            "step text",
            singletonList("snippet"),
            emptyList());
        assertThat(exception.getMessage(), is("" +
                "The step \"step text\" is undefined. You can implement it using the snippet(s) below:\n" +
                "\n" +
                "snippet\n"));
    }

    @Test
    void should_generate_a_message_for_step_with_additional_snippets() {
        UndefinedStepException exception = new UndefinedStepException(
            "step text",
            singletonList("snippet"),
            singletonList(singletonList("additional snippet")));
        assertThat(exception.getMessage(), is("" +
                "The step \"step text\" is undefined. You can implement it using the snippet(s) below:\n" +
                "\n" +
                "snippet\n" +
                "\n" +
                "\n" +
                "Some other steps were also undefined:\n" +
                "\n" +
                "additional snippet\n"));
    }

    @Test
    void should_generate_a_message_for_step_with_additional_duplicated_snippets() {
        UndefinedStepException exception = new UndefinedStepException(
            "step text",
            singletonList("snippet"),
            singletonList(singletonList("snippet")));
        assertThat(exception.getMessage(), is("" +
                "The step \"step text\" is undefined. You can implement it using the snippet(s) below:\n" +
                "\n" +
                "snippet\n"));
    }

}
