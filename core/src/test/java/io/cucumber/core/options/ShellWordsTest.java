package io.cucumber.core.options;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ShellWordsTest {

    @Test
    public void parses_single_quoted_strings() {
        assertThat(ShellWords.parse("--name 'The Fox'"), is(equalTo(asList("--name", "The Fox"))));
    }

    @Test
    public void parses_double_quoted_strings() {
        assertThat(ShellWords.parse("--name \"The Fox\""), is(equalTo(asList("--name", "The Fox"))));
    }

    @Test
    public void parses_both_single_and_double_quoted_strings() {
        assertThat(ShellWords.parse("--name \"The Fox\" --fur 'Brown White'"), is(equalTo(asList("--name", "The Fox", "--fur", "Brown White"))));
    }

    @Test
    public void can_quote_both_single_and_double_quotes() {
        assertThat(ShellWords.parse("\"'\" '\"'"), is(equalTo(asList("'", "\""))));
    }

}
