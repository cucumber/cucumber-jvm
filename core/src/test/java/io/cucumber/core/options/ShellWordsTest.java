package io.cucumber.core.options;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ShellWordsTest {

    @Test
    public void parses_single_quoted_strings() {
        assertEquals(asList("--name", "The Fox"), ShellWords.parse("--name 'The Fox'"));
    }

    @Test
    public void parses_double_quoted_strings() {
        assertEquals(asList("--name", "The Fox"), ShellWords.parse("--name \"The Fox\""));
    }

    @Test
    public void parses_both_single_and_double_quoted_strings() {
        assertEquals(asList("--name", "The Fox", "--fur", "Brown White"), ShellWords.parse("--name \"The Fox\" --fur 'Brown White'"));
    }

    @Test
    public void can_quote_both_single_and_double_quotes() {
        assertEquals(asList("'", "\""), ShellWords.parse("\"'\" '\"'"));
    }

}
