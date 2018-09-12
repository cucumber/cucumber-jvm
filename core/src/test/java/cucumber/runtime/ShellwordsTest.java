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
                "--alfa",
                asList("--alfa")
            },

            // ---name whitespace padded
            new Object[]{
                "     --bravo     ",
                asList("--bravo")
            },

            // ---name --fur
            new Object[]{
                "--charlie --delta",
                asList("--charlie", "--delta")
            },

            // ---name --fur whitespace padding
            new Object[]{
                "     --echo     --foxtrot     ",
                asList("--echo", "--foxtrot")
            },

            // parses_single_quoted_strings
            new Object[]{
                "--golf 'Hotel India'",
                asList("--golf", "Hotel India")
            },

            // parses_single_quoted_strings whitespace padding
            new Object[]{
                "     --juliett     'Kilo Lima'     ",
                asList("--juliett", "Kilo Lima")
            },

            // parses_double_quoted_strings
            new Object[]{
                "--mike \"November Oscar\"",
                asList("--mike", "November Oscar")
            },

            // parses_double_quoted_strings whitespace padding
            new Object[]{
                "     --papa      \"Quebec Romeo\"     ",
                asList("--papa", "Quebec Romeo")
            },

            // double_quote start continue until double_quote end, and ignore single_quote early termination
            new Object[]{
                "--sierra \"Tango ' Uniform\"",
                asList("--sierra", "Tango ' Uniform")
            },
            new Object[]{
                "--victor \"Whiskey 'xray yankee' Zulu\"",
                asList("--victor", "Whiskey 'xray yankee' Zulu")
            },

            // single_quote start continue until single_quote end, and ignore double_quote early termination
            new Object[]{
                "--alfa 'Bravo \" Charlie'",
                asList("--alfa", "Bravo \" Charlie")
            },
            new Object[]{
                "--delta 'Echo \"foxtrot golf\" hotel'",
                asList("--delta", "Echo \"foxtrot golf\" hotel")
            },

            // double_quotes wrapped by single_quote
            new Object[]{
                "--india '\"Juliett Kilo\"'",
                asList("--india", "\"Juliett Kilo\"")
            },

            // single_quote wrapped by double_quotes
            new Object[]{
                "--lima \"'Mike November'\"",
                asList("--lima", "'Mike November'")
            },

            // parses_both_single_and_double_quoted_strings
            new Object[]{
                "--oscar \"Papa Quebec\" --romeo 'Sierra Tango'",
                asList("--oscar", "Papa Quebec", "--romeo", "Sierra Tango")
            },

            // triple double_quote
            // TODO is this correct with trailing empty string
            new Object[]{
                "--uniform \"Victor Whiskey --xray \"Yankee Zulu\"",
                asList("--uniform", "Victor Whiskey --xray ", "Yankee", "Zulu", "")
            },

            // opening double_quote only with following single_quotes
            new Object[]{
                "--alfa \"Bravo Charlie --delta 'Echo Foxtrot'",
                asList("--alfa", "Bravo Charlie --delta 'Echo Foxtrot'")
            },

            // triple single_quotes
            // TODO is this correct with trailing empty string
            new Object[]{
                "--golf 'Hotel India --juliett 'Kilo Lima'",
                asList("--golf", "Hotel India --juliett ", "Kilo", "Lima", "")
            },

            // opening single_quotes only with following double_quotes
            new Object[]{
                "--mike 'November Oscar --papa \"Quebec Romeo\"",
                asList("--mike", "November Oscar --papa \"Quebec Romeo\"")
            },

            // double_quotes within word
            new Object[]{
                "sierra\"tango",
                asList("sierra", "tango")
            },

            // single_quotes within word
            new Object[]{
                "uniform'victor",
                asList("uniform", "victor")
            },

            // can_quote_both_single_and_double_quotes
            new Object[]{
                "\"'\" '\"'",
                asList("'", "\"")
            }

        );
    }

}
