package io.cucumber.testng;

import io.cucumber.core.runtime.TestCaseResultObserver.Suggestion;
import io.cucumber.plugin.event.Location;
import org.testng.annotations.Test;

import java.net.URI;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;

public class UndefinedStepExceptionTest {

    private final URI uri = URI.create("classpath:example.feature");
    private final Location stepLocation = new Location(12, 4);

    @Test
    public void should_generate_a_message_for_no_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(emptyList());
        assertThat(exception.getMessage(), is("This step is undefined"));
    }

    @Test
    void should_generate_an_empty_stacktrace_for_no_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(emptyList());
        assertThat(exception.getStackTrace(), arrayWithSize(0));
    }

    @Test
    public void should_generate_a_message_for_one_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            singletonList(
                new Suggestion("some step", singletonList("some snippet"), uri, stepLocation))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' is undefined.\n" +
                "You can implement this step using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n"));
    }

    @Test
    void should_generate_a_stacktrace_for_one_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            singletonList(
                new Suggestion("some step", singletonList("some snippet"), uri, stepLocation))

        );
        assertThat(exception.getStackTrace(), arrayWithSize(1));
        assertThat(exception.getStackTrace()[0].toString(), equalTo("✽.some step(classpath:example.feature:12)"));
    }

    @Test
    public void should_generate_a_message_for_one_suggestions_with_multiple_snippets() {
        UndefinedStepException exception = new UndefinedStepException(
            singletonList(
                new Suggestion("some step", asList("some snippet", "some other snippet"), uri,
                    stepLocation))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' is undefined.\n" +
                "You can implement this step using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n" +
                "some other snippet\n"));
    }

    @Test
    public void should_generate_a_message_for_two_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            asList(
                new Suggestion("some step", singletonList("some snippet"), uri, stepLocation),
                new Suggestion("some other step", singletonList("some other snippet"), uri,
                    stepLocation))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' and 1 other step(s) are undefined.\n" +
                "You can implement these steps using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n" +
                "some other snippet\n"));
    }

    @Test
    public void should_generate_a_message_without_duplicate_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            asList(
                new Suggestion("some step", asList("some snippet", "some snippet"), uri,
                    stepLocation),
                new Suggestion("some other step", asList("some other snippet", "some other snippet"), uri,
                    stepLocation))

        );
        assertThat(exception.getMessage(), is("" +
                "The step 'some step' and 1 other step(s) are undefined.\n" +
                "You can implement these steps using the snippet(s) below:\n" +
                "\n" +
                "some snippet\n" +
                "some other snippet\n"));
    }

    @Test
    public void should_generate_a_message_for_three_suggestions() {
        UndefinedStepException exception = new UndefinedStepException(
            asList(
                new Suggestion("some step", singletonList("some snippet"), uri, stepLocation),
                new Suggestion("some other step", singletonList("some other snippet"), uri,
                    stepLocation),
                new Suggestion("yet another step", singletonList("yet another snippet"), uri,
                    stepLocation))

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
