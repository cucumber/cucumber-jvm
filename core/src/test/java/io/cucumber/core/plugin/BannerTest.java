package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BannerTest {

    @Test
    void printsAnsiBanner() throws UnsupportedEncodingException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes, false, StandardCharsets.UTF_8.name()), false);

        banner.print(asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")), AnsiEscapes.CYAN);

        assertThat(bytes, isBytesEqualTo("" +
                "\u001B[36m┌─────────────┐\u001B[0m\n" +
                "\u001B[36m│\u001B[0m Bla         \u001B[36m│\u001B[0m\n" +
                "\u001B[36m│\u001B[0m Bla \u001B[34mBla\u001B[0m \u001B[31mBla\u001B[0m \u001B[36m│\u001B[0m\n" +
                "\u001B[36m│\u001B[0m Bla Bla     \u001B[36m│\u001B[0m\n" +
                "\u001B[36m└─────────────┘\u001B[0m\n"));
    }

    @Test
    void printsMonochromeBanner() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes, false, StandardCharsets.UTF_8.name()), true);

        banner.print(asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")), AnsiEscapes.CYAN);

        assertThat(bytes, isBytesEqualTo("" +
                "┌─────────────┐\n" +
                "│ Bla         │\n" +
                "│ Bla Bla Bla │\n" +
                "│ Bla Bla     │\n" +
                "└─────────────┘\n"));
    }

}
