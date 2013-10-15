package cucumber.runtime;

import org.junit.Ignore;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ShellwordsTest {
    @Test
    public void parses_single_quoted_strings() {
        assertEquals(asList("--name", "The Fox"), Shellwords.parse("--name 'The Fox'"));
    }

    @Ignore("TODO: fixme")
    @Test
    public void parses_double_quoted_strings() {
        assertEquals(asList("--name", "The Fox"), Shellwords.parse("--name \"The Fox\""));
    }

    @Ignore("TODO: fixme")
    @Test
    public void parses_both_single_and_double_quoted_strings() {
        assertEquals(asList("--name", "The Fox", "--fur", "Brown White"), Shellwords.parse("--name \"The Fox\" --fur 'Brown White'"));
    }

    @Ignore("TODO: fixme")
    @Test
    public void can_quote_both_single_and_double_quotes() {
        assertEquals(asList("'", "\""), Shellwords.parse("\"'\" '\"'"));
    }
}
