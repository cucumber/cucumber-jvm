package io.cucumber.junit.platform.engine;

import io.cucumber.core.runtime.TestCaseResultObserver.Suggestion;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UndefinedStepExceptionTest {

    @Test
    void should_generate_a_message_for_no_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(emptyList());
        assertThat(exception.getMessage(), is("This step is undefined"));
    }

    @Test
    void should_generate_a_message_for_one_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            singletonList(
                new Suggestion("some step", singletonList("some snippet")))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' is undefined.\n" +
                "You can implement this step using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n"));
    }

    @Test
    void should_generate_a_message_for_one_suggestions_with_multiple_snippets() {
        UndefinedStepException exception = new UndefinedStepException(
            singletonList(
                new Suggestion("some step", asList("some snippet", "some other snippet")))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' is undefined.\n" +
                "You can implement this step using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n" +
                "some other snippet\n"));
    }

    @Test
    void should_generate_a_message_for_two_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            asList(
                new Suggestion("some step", singletonList("some snippet")),
                new Suggestion("some other step", singletonList("some other snippet")))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' and 1 other step(s) are undefined.\n" +
                "You can implement these steps using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n" +
                "some other snippet\n"));
    }

    @Test
    void should_generate_a_message_without_duplicate_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            asList(
                new Suggestion("some step", asList("some snippet", "some snippet")),
                new Suggestion("some other step", asList("some other snippet", "some other snippet")))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' and 1 other step(s) are undefined.\n" +
                "You can implement these steps using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n" +
                "some other snippet\n"));
    }

    @Test
    void should_generate_a_message_for_three_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            asList(
                new Suggestion("some step", singletonList("some snippet")),
                new Suggestion("some other step", singletonList("some other snippet")),
                new Suggestion("yet another step", singletonList("yet another snippet")))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' and 2 other step(s) are undefined.\n" +
                "You can implement these steps using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n" +
                "some other snippet\n" +
                "yet another snippet\n"));
    }

}
