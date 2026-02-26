package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static io.cucumber.core.plugin.Bytes.bytes;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class BannerTest {

    @Test
    void printsAnsiBanner() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes, false, UTF_8), false);

        banner.print(asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")), AnsiEscapes.CYAN);

        assertThat(bytes, bytes(equalTo("""
                \u001B[36m┌─────────────┐\u001B[0m
                \u001B[36m│\u001B[0m Bla         \u001B[36m│\u001B[0m
                \u001B[36m│\u001B[0m Bla \u001B[34mBla\u001B[0m \u001B[31mBla\u001B[0m \u001B[36m│\u001B[0m
                \u001B[36m│\u001B[0m Bla Bla     \u001B[36m│\u001B[0m
                \u001B[36m└─────────────┘\u001B[0m
                """)));
    }

    @Test
    void printsMonochromeBanner() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes, false, UTF_8), true);

        banner.print(asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")), AnsiEscapes.CYAN);

        assertThat(bytes, bytes(equalTo("""
                ┌─────────────┐
                │ Bla         │
                │ Bla Bla Bla │
                │ Bla Bla     │
                └─────────────┘
                """)));
    }

}
