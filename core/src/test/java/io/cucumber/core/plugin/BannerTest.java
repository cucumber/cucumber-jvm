package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BannerTest {

    @Test
    void printsAnsiBanner() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes), false);

        banner.print(AnsiEscapes.CYAN, asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")));

        assertThat(bytes.toString("UTF-8"), is("" +
                "\u001B[36m┌─────────────┐\u001B[0m\n" +
                "\u001B[36m│\u001B[0m Bla         \u001B[36m│\u001B[0m\n" +
                "\u001B[36m│\u001B[0m Bla \u001B[34mBla\u001B[0m \u001B[31mBla\u001B[0m \u001B[36m│\u001B[0m\n" +
                "\u001B[36m│\u001B[0m Bla Bla     \u001B[36m│\u001B[0m\n" +
                "\u001B[36m└─────────────┘\u001B[0m\n"));
    }

    @Test
    void printsMonochromeBanner() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes), true);

        banner.print(AnsiEscapes.CYAN, asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")));

        assertThat(bytes.toString("UTF-8"), is("" +
                "┌─────────────┐\n" +
                "│ Bla         │\n" +
                "│ Bla Bla Bla │\n" +
                "│ Bla Bla     │\n" +
                "└─────────────┘\n"));
    }

}
