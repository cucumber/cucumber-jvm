package cucumber.runtime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Parameterized.class)
public class ShellwordsTest {

    private final List<String> expected;
    private final String input;

    public ShellwordsTest(final String input, final List<String> expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void parseTest() {
        assertThat(Shellwords.parse(this.input), is(equalTo(this.expected)));
    }

    @Parameterized.Parameters(name = "{index}: input={0}, expected={1}")
    public static Collection<Object[]> data() {

        return Arrays.asList(

            // template
            // new Object[]{
            //    input String,
            //    expected List<String>
            // },

            // empty string
            new Object[]{
                "",
                new ArrayList<>()
            },

            // whitespace string
            new Object[]{
                "    ",
                new ArrayList<>()
            },

            // ---name only
            new Object[]{
                "--name",
                asList("--name")
            },

            // ---name whitespace padded
            new Object[]{
                "     --name     ",
                asList("--name")
            },

            // ---name --fur
            new Object[]{
                "--name --fur",
                asList("--name", "--fur")
            },

            // ---name --fur whitespace padding
            new Object[]{
                "     --name     --fur     ",
                asList("--name", "--fur")
            },

            // parses_single_quoted_strings
            new Object[]{
                "--name 'The Fox'",
                asList("--name", "The Fox")
            },

            // parses_single_quoted_strings whitespace padding
            new Object[]{
                "     --name     'The Fox'     ",
                asList("--name", "The Fox")
            },

            // parses_double_quoted_strings
            new Object[]{
                "--name \"The Fox\"",
                asList("--name", "The Fox")
            },

            // parses_double_quoted_strings whitespace padding
            new Object[]{
                "     --name      \"The Fox\"     ",
                asList("--name", "The Fox")
            },

            // double_quote start continue until double_quote end, and ignore single_quote early termination
            new Object[]{
                "--name \"The ' Fox\"",
                asList("--name", "The ' Fox")
            },

            // single_quote start continue until single_quote end, and ignore double_quote early termination
            new Object[]{
                "--name 'The \" Fox'",
                asList("--name", "The \" Fox")
            },

            // parses_both_single_and_double_quoted_strings
            new Object[]{
                "--name \"The Fox\" --fur 'Brown White'",
                asList("--name", "The Fox", "--fur", "Brown White")
            },

            // can_quote_both_single_and_double_quotes
            new Object[]{
                "\"'\" '\"'",
                asList("'", "\"")
            }

        );
    }

}
